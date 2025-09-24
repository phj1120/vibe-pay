package com.vibe.pay.backend.payment;

import com.vibe.pay.backend.enums.OrderStatus;
import com.vibe.pay.backend.enums.PayType;
import com.vibe.pay.backend.paymentlog.PaymentInterfaceRequestLog;
import com.vibe.pay.backend.paymentlog.PaymentInterfaceRequestLogMapper;
import com.vibe.pay.backend.order.OrderMapper;
import com.vibe.pay.backend.pointhistory.PointHistoryService;
import com.vibe.pay.backend.payment.factory.PaymentProcessorFactory;
import com.vibe.pay.backend.payment.factory.PaymentGatewayFactory;
import com.vibe.pay.backend.payment.processor.PaymentProcessor;
import com.vibe.pay.backend.payment.gateway.PaymentGatewayAdapter;
import com.vibe.pay.backend.payment.gateway.PaymentInitResponse;
import com.vibe.pay.backend.payment.gateway.PaymentConfirmResponse;
import com.vibe.pay.backend.exception.PaymentException;
import com.vibe.pay.backend.enums.PaymentMethod;
import com.vibe.pay.backend.enums.PgCompany;
import com.vibe.pay.backend.common.Constants;
import com.vibe.pay.backend.util.HashUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import java.security.MessageDigest;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.MultiValueMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;


@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentMapper paymentMapper;
    private final PaymentInterfaceRequestLogMapper paymentInterfaceRequestLogMapper;
    private final OrderMapper orderMapper;
    private final PointHistoryService pointHistoryService;
    private final ObjectMapper objectMapper;
    private final PaymentProcessorFactory paymentProcessorFactory;
    private final PaymentGatewayFactory paymentGatewayFactory;


    public Optional<Payment> getPaymentById(String paymentId) {
        return Optional.ofNullable(paymentMapper.findByPaymentId(paymentId));
    }

    public List<Payment> findByOrderId(String orderId) {
        return paymentMapper.findByOrderId(orderId);
    }

    public List<Payment> getAllPayments() {
        return paymentMapper.findAll();
    }

    public Payment updatePayment(String paymentId, Payment paymentDetails) {
        Payment existingPayment = paymentMapper.findByPaymentId(paymentId);
        if (existingPayment == null) {
            throw PaymentException.approvalFailed("Payment not found with id " + paymentId);
        }
        paymentDetails.setPaymentId(paymentId);
        paymentMapper.update(paymentDetails);
        return paymentDetails;
    }

    public void deletePayment(String paymentId) {
        Payment payment = paymentMapper.findByPaymentId(paymentId);
        if (payment == null) {
            throw PaymentException.approvalFailed("Payment not found with id " + paymentId);
        }
        paymentMapper.delete(payment);
    }

    @Transactional
    public PaymentInitResponse initiatePayment(PaymentInitiateRequest request) {
        log.info("Initiating payment with Factory/Adapter pattern: orderId={}, method={}, pgCompany={}",
                request.getOrderId(), request.getPaymentMethod(), "INICIS");

        try {
            // 1. PG 어댑터 선택 (현재는 INICIS만 지원, 추후 확장 가능)
            PaymentGatewayAdapter pgAdapter = paymentGatewayFactory.getAdapter(PgCompany.INICIS.getCode());
            
            // 2. PG 결제 시작 처리
            PaymentInitResponse pgResponse = pgAdapter.initiate(request);
            
            if (!pgResponse.isSuccess()) {
                throw PaymentException.initiationFailed(pgResponse.getErrorMessage());
            }

            log.info("Payment initiation completed: orderId={}, paymentId={}", 
                    request.getOrderId(), pgResponse.getPaymentId());
            
            return pgResponse;

        } catch (PaymentException e) {
            log.error("Payment initiation failed: {}", e.getErrorMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during payment initiation: {}", e.getMessage(), e);
            throw PaymentException.pgSystemError("PAYMENT_SYSTEM", e);
        }
    }

    @Transactional
    public Payment confirmPayment(PaymentConfirmRequest request) {
        log.info("Processing payment confirmation with Factory/Adapter pattern: orderId={}, method={}",
                request.getOrderId(), request.getPaymentMethod());

        try {
            // 1. 결제수단별 Processor 선택 및 처리 (PG 승인 포함)
            PaymentProcessor processor = paymentProcessorFactory.getProcessor(request.getPaymentMethod());
            Payment payment = processor.processPayment(request);

            // 2. 포인트 사용 내역 기록 (포인트를 실제로 사용한 경우만)
            Long usedPoints = request.getUsedPoints();
            if (usedPoints > 0) {
                try {
                    pointHistoryService.recordPointUsage(
                            request.getMemberId(),
                            usedPoints,
                            payment.getPaymentId(),
                            "결제 시 포인트 사용 - 주문번호: " + request.getOrderId()
                    );
                    log.info("Point usage recorded: memberId={}, usedPoints={}, paymentId={}",
                            request.getMemberId(), usedPoints, payment.getPaymentId());

                    // 포인트 결제 별도 Payment 레코드 생성
                    createPointPaymentRecord(request.getMemberId(), request.getOrderId(), usedPoints);
                } catch (Exception e) {
                    log.error("Failed to record point usage: {}", e.getMessage(), e);
                    // 포인트 히스토리 기록 실패해도 결제는 성공으로 처리
                }
            }

            log.info("Payment processed successfully: paymentId={}, method={}, amount={}",
                    payment.getPaymentId(), payment.getPaymentMethod(), payment.getAmount());

            return payment;

        } catch (PaymentException e) {
            log.error("Payment confirmation failed: {}", e.getErrorMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during payment confirmation: {}", e.getMessage(), e);
            throw PaymentException.pgSystemError("PAYMENT_SYSTEM", e);
        }
    }
    
    // JSON 변환 유틸리티 메소드
    private String convertToJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert object to JSON: {}", obj, e);
            return "{}";
        }
    }

    // Payment ID 생성 메소드 (17자리 고정 형식)
    private String generatePaymentId() {
        // Payment ID 형식: YYYYMMDDP + 8자리 시퀀스 (총 17자리)
        // 예: 20250918P00000001

        // 현재 날짜를 YYYYMMDD 형식으로 가져오기
        java.time.LocalDate today = java.time.LocalDate.now();
        String datePrefix = today.format(Constants.DATE_FORMATTER_YYYYMMDD);

        // 시퀀스 번호 가져오기 (8자리로 패딩)
        Long sequence = paymentMapper.getNextPaymentSequence();
        String sequenceStr = String.format("%0" + Constants.SEQUENCE_PADDING_LENGTH + "d", sequence);

        // 최종 Payment ID 생성: YYYYMMDDP + 8자리 시퀀스
        return datePrefix + Constants.PAYMENT_ID_PREFIX + sequenceStr;
    }

    /**
     * 포인트 결제 건을 위한 별도 Payment 레코드 생성
     */
    @Transactional
    public void createPointPaymentRecord(Long memberId, String orderId, Long usedPoints) {
        try {
            // 포인트 결제용 Payment ID 생성
            String pointPaymentId = generatePaymentId();

            Payment pointPayment = new Payment();

            pointPayment.setMemberId(memberId);
            pointPayment.setOrderId(orderId);
            pointPayment.setPaymentMethod(PaymentMethod.POINT.getCode());
            pointPayment.setPayType(PayType.PAYMENT.getCode());
            pointPayment.setStatus("SUCCESS");

            pointPayment.setPaymentId(pointPaymentId);
            pointPayment.setOrderStatus(OrderStatus.ORDERED.getCode()); // 주문시 ORDER
            paymentMapper.insert(pointPayment);

            log.info("Point payment record created: paymentId={}, memberId={}, order={}, usedPoints={}",
                    pointPaymentId, memberId, orderId, usedPoints);

        } catch (Exception e) {
            log.error("Failed to create point payment record: memberId={}, orderId={}, usedPoints={}",
                    memberId, orderId, usedPoints, e);
        }
    }
}