package com.vibe.pay.backend.payment.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibe.pay.backend.payment.Payment;
import com.vibe.pay.backend.payment.PaymentMapper;
import com.vibe.pay.backend.payment.dto.*;
import com.vibe.pay.backend.paymentlog.PaymentInterfaceRequestLogService;
import com.vibe.pay.backend.util.WebClientUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Toss 결제 게이트웨이 어댑터
 *
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TossAdapter implements PaymentGateway {

    private final PaymentMapper paymentMapper;
    private final PaymentInterfaceRequestLogService logService;
    private final WebClientUtil webClientUtil;
    private final ObjectMapper objectMapper;

    @Value("${toss.clientKey:test_clientkey}")
    private String clientKey;

    @Value("${toss.secretKey:test_secretkey}")
    private String secretKey;

    @Value("${toss.successUrl:http://localhost:3000/order/complete}")
    private String successUrl;

    @Value("${toss.failUrl:http://localhost:3000/order/popup}")
    private String failUrl;

    @Override
    public PaymentInitResponse initiate(PaymentInitRequest request) {
        log.debug("Toss 결제 초기화: orderId={}, amount={}", request.getOrderId(), request.getAmount());

        try {
            // 결제 ID 생성
            String paymentId = generatePaymentId();

            // Payment 엔티티 생성 (초기 상태)
            Payment payment = Payment.builder()
                    .paymentId(paymentId)
                    .memberId(request.getMemberId())
                    .orderId(request.getOrderId())
                    .amount(request.getAmount())
                    .paymentMethod("CREDIT_CARD")
                    .payType("PAYMENT")
                    .pgCompany("TOSS")
                    .status("PENDING")
                    .orderStatus("INIT")
                    .paymentDate(LocalDateTime.now())
                    .build();

            paymentMapper.insert(payment);

            // 응답 생성 (Toss는 클라이언트 사이드에서 처리)
            PaymentInitResponse response = PaymentInitResponse.builder()
                    .success(true)
                    .paymentId(paymentId)
                    .selectedPgCompany("TOSS")
                    .oid(request.getOrderId())
                    .price(String.valueOf(request.getAmount().intValue()))
                    .goodName(request.getGoodName())
                    .build();

            log.info("Toss 결제 초기화 완료: paymentId={}", paymentId);

            return response;

        } catch (Exception e) {
            log.error("Toss 결제 초기화 실패: orderId={}", request.getOrderId(), e);
            return PaymentInitResponse.builder()
                    .success(false)
                    .errorMessage("Toss 결제 초기화 실패: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public Payment confirm(PaymentConfirmRequest request) {
        log.debug("Toss 결제 승인: orderId={}", request.getOrderId());

        try {
            // Toss 승인 요청
            Map<String, Object> confirmRequest = new HashMap<>();
            confirmRequest.put("orderId", request.getOrderId());
            confirmRequest.put("amount", request.getPrice().intValue());
            confirmRequest.put("paymentKey", request.getTxTid());

            String requestJson = objectMapper.writeValueAsString(confirmRequest);
            log.debug("Toss 승인 요청: {}", requestJson);

            // Authorization 헤더 생성
            String auth = Base64.getEncoder().encodeToString((secretKey + ":").getBytes());
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Basic " + auth);

            // PG 승인 API 호출
            String apiUrl = "https://api.tosspayments.com/v1/payments/confirm";
            String responseJson = webClientUtil.post(apiUrl, confirmRequest, headers);
            log.debug("Toss 승인 응답: {}", responseJson);

            // 응답 파싱
            JsonNode responseNode = objectMapper.readTree(responseJson);

            // 로그 저장
            String paymentId = generatePaymentId();
            logService.logRequest(paymentId, "TOSS_CONFIRM", requestJson, responseJson);

            // Payment 엔티티 생성
            Payment payment = Payment.builder()
                    .paymentId(paymentId)
                    .memberId(request.getMemberId())
                    .orderId(request.getOrderId())
                    .amount(request.getPrice())
                    .paymentMethod("CREDIT_CARD")
                    .payType("PAYMENT")
                    .pgCompany("TOSS")
                    .status("APPROVED")
                    .orderStatus("ORDER")
                    .transactionId(responseNode.path("paymentKey").asText())
                    .paymentDate(LocalDateTime.now())
                    .build();

            paymentMapper.insert(payment);

            log.info("Toss 결제 승인 완료: paymentId={}", paymentId);

            return payment;

        } catch (Exception e) {
            log.error("Toss 결제 승인 실패: orderId={}", request.getOrderId(), e);
            throw new RuntimeException("Toss 결제 승인 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public Payment cancel(PaymentCancelRequest request) {
        log.debug("Toss 결제 취소: paymentId={}, transactionId={}", request.getPaymentId(), request.getTransactionId());

        try {
            // 취소 요청 데이터 생성
            Map<String, Object> cancelRequest = new HashMap<>();
            cancelRequest.put("cancelReason", request.getReason() != null ? request.getReason() : "고객 요청");

            String requestData = objectMapper.writeValueAsString(cancelRequest);
            log.debug("Toss 취소 요청: {}", requestData);

            // Authorization 헤더 생성
            String auth = Base64.getEncoder().encodeToString((secretKey + ":").getBytes());
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Basic " + auth);

            // PG 취소 API 호출
            String apiUrl = "https://api.tosspayments.com/v1/payments/" + request.getTransactionId() + "/cancel";
            String responseJson = webClientUtil.post(apiUrl, cancelRequest, headers);
            log.debug("Toss 취소 응답: {}", responseJson);

            // 로그 저장
            logService.logRequest(request.getPaymentId(), "TOSS_CANCEL", requestData, responseJson);

            // Payment 상태 업데이트
            Payment payment = paymentMapper.selectByPaymentId(request.getPaymentId());
            payment.setStatus("CANCELLED");
            payment.setOrderStatus("CANCEL");
            paymentMapper.update(payment);

            log.info("Toss 결제 취소 완료: paymentId={}", request.getPaymentId());

            return payment;

        } catch (Exception e) {
            log.error("Toss 결제 취소 실패: paymentId={}", request.getPaymentId(), e);
            throw new RuntimeException("Toss 결제 취소 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public Payment netCancel(PaymentNetCancelRequest request) {
        log.debug("Toss 망취소: paymentId={}, transactionId={}", request.getPaymentId(), request.getTransactionId());

        try {
            // 망취소 요청 데이터 생성
            Map<String, Object> cancelRequest = new HashMap<>();
            cancelRequest.put("cancelReason", "주문 생성 실패 - 망취소");

            String requestData = objectMapper.writeValueAsString(cancelRequest);
            log.debug("Toss 망취소 요청: {}", requestData);

            // Authorization 헤더 생성
            String auth = Base64.getEncoder().encodeToString((secretKey + ":").getBytes());
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Basic " + auth);

            // PG 망취소 API 호출
            String apiUrl = "https://api.tosspayments.com/v1/payments/" + request.getTransactionId() + "/cancel";
            String responseJson = webClientUtil.post(apiUrl, cancelRequest, headers);
            log.debug("Toss 망취소 응답: {}", responseJson);

            // 로그 저장
            logService.logRequest(request.getPaymentId(), "TOSS_NET_CANCEL", requestData, responseJson);

            // Payment 상태 업데이트
            Payment payment = paymentMapper.selectByPaymentId(request.getPaymentId());
            payment.setStatus("NET_CANCELLED");
            payment.setOrderStatus("NET_CANCEL");
            paymentMapper.update(payment);

            log.info("Toss 망취소 완료: paymentId={}", request.getPaymentId());

            return payment;

        } catch (Exception e) {
            log.error("Toss 망취소 실패: paymentId={}", request.getPaymentId(), e);
            throw new RuntimeException("Toss 망취소 실패: " + e.getMessage(), e);
        }
    }

    private String generatePaymentId() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String seq = String.format("%09d", System.currentTimeMillis() % 1000000000);
        return date + "P" + seq;
    }
}
