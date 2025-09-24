package com.vibe.pay.backend.payment.processor;

import com.vibe.pay.backend.common.Constants;
import com.vibe.pay.backend.enums.OrderStatus;
import com.vibe.pay.backend.enums.PaymentMethod;
import com.vibe.pay.backend.enums.PayType;
import com.vibe.pay.backend.enums.PaymentStatus;
import com.vibe.pay.backend.payment.Payment;
import com.vibe.pay.backend.payment.PaymentConfirmRequest;
import com.vibe.pay.backend.payment.PaymentMapper;
import com.vibe.pay.backend.payment.dto.PaymentNetCancelRequest;
import com.vibe.pay.backend.pointhistory.PointHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PointPaymentProcessor implements PaymentProcessor {

    private static final Logger log = LoggerFactory.getLogger(PointPaymentProcessor.class);

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private PointHistoryService pointHistoryService;

    @Override
    public Payment processPayment(PaymentConfirmRequest request) {
        log.info("Processing point payment for order: {}", request.getOrderId());

        String paymentId = generatePaymentId();

        // 포인트 결제 기록 생성
        Payment payment = new Payment();
        payment.setPaymentId(paymentId);
        payment.setOrderId(request.getOrderId());
        payment.setClaimId(null);
        payment.setMemberId(request.getMemberId());
        payment.setAmount(request.getPrice());
        payment.setPaymentMethod(PaymentMethod.POINT.getCode());
        payment.setPayType(PayType.PAYMENT.getCode());
        payment.setPgCompany(null);
        payment.setStatus(PaymentStatus.SUCCESS.getCode());
        payment.setTransactionId(request.getTransactionId());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setOrderStatus(OrderStatus.ORDERED.getCode());

        paymentMapper.insert(payment);

        pointHistoryService.recordPointRefund(
                payment.getMemberId(),
                -payment.getAmount(),
                payment.getPaymentId(),
                "주문 취소로 인한 포인트 환불 - 주문번호: " + payment.getOrderId()
        );

        return payment;
    }

    @Override
    public Payment processRefund(Payment originalPayment) {
        String paymentId = generatePaymentId();

        // 포인트 환불 기록 생성
        Payment refundPayment = new Payment();
        refundPayment.setPaymentId(paymentId);
        refundPayment.setOrderId(originalPayment.getOrderId());
        refundPayment.setClaimId(originalPayment.getClaimId());
        refundPayment.setMemberId(originalPayment.getMemberId());
        refundPayment.setAmount(originalPayment.getAmount());
        refundPayment.setPaymentMethod(PaymentMethod.POINT.getCode());
        refundPayment.setPayType(PayType.REFUND.getCode());
        refundPayment.setPgCompany(null);
        refundPayment.setStatus(PaymentStatus.SUCCESS.getCode());
        refundPayment.setTransactionId(originalPayment.getTransactionId());
        refundPayment.setPaymentDate(LocalDateTime.now());
        refundPayment.setOrderStatus(OrderStatus.CANCELLED.getCode());
        paymentMapper.insert(refundPayment);

        pointHistoryService.recordPointRefund(
                originalPayment.getMemberId(),
                originalPayment.getAmount(),
                originalPayment.getPaymentId(),
                "주문 취소로 인한 포인트 환불 - 주문번호: " + originalPayment.getOrderId()
        );

        return refundPayment;
    }

    @Override
    public void netCancel(PaymentNetCancelRequest paymentNetCancelRequest) {
    }

    @Override
    public boolean canProcess(String paymentMethod) {
        return PaymentMethod.POINT.getCode().equals(paymentMethod);
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
}