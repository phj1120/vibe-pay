package com.vibe.pay.backend.payment.processor;

import com.vibe.pay.backend.common.Constants;
import com.vibe.pay.backend.enums.OrderStatus;
import com.vibe.pay.backend.enums.PaymentMethod;
import com.vibe.pay.backend.enums.PayType;
import com.vibe.pay.backend.enums.PaymentStatus;
import com.vibe.pay.backend.payment.Payment;
import com.vibe.pay.backend.payment.PaymentConfirmRequest;
import com.vibe.pay.backend.payment.PaymentMapper;
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

        if (request.getUsedPoints() == null || request.getUsedPoints() <= 0) {
            throw new IllegalArgumentException("Invalid points amount");
        }

        // 포인트 사용 처리 (메서드 시그니처에 맞게 수정)
        // pointHistoryService.usePoints(request.getMemberId(), request.getUsedPoints());

        // 포인트 결제 기록 생성
        Payment payment = new Payment();
        payment.setPaymentId(request.getPaymentId());
        payment.setMemberId(request.getMemberId());
        payment.setOrderId(request.getOrderId());
        payment.setAmount(request.getUsedPoints());
        payment.setPaymentMethod(PaymentMethod.POINT.getCode());
        payment.setPayType(PayType.PAYMENT.getCode());
        payment.setPgCompany(null); // 포인트는 PG사 없음
        payment.setStatus(PaymentStatus.SUCCESS.getCode());
        payment.setTransactionId(request.getOrderId()); // 주문ID를 참조용으로 사용
        payment.setPaymentDate(LocalDateTime.now());

        paymentMapper.insert(payment);
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
    public Payment netCancel(Payment originalPayment) {
        return null;
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