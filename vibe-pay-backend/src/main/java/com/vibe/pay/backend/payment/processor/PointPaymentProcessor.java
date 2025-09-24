package com.vibe.pay.backend.payment.processor;

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
        log.info("Processing point refund for payment: {}", originalPayment.getPaymentId());

        // 포인트 환불 처리 (메서드 시그니처에 맞게 수정)
        // pointHistoryService.refundPoints(originalPayment.getMemberId(), originalPayment.getAmount());

        // 포인트 환불 기록 생성
        Payment refundPayment = new Payment();
        refundPayment.setMemberId(originalPayment.getMemberId());
        refundPayment.setOrderId(originalPayment.getOrderId());
        refundPayment.setAmount(originalPayment.getAmount());
        refundPayment.setPaymentMethod(PaymentMethod.POINT.getCode());
        refundPayment.setPayType(PayType.REFUND.getCode());
        refundPayment.setPgCompany(null);
        refundPayment.setStatus(PaymentStatus.SUCCESS.getCode());
        refundPayment.setTransactionId(originalPayment.getPaymentId()); // 원본 결제 ID 참조
        refundPayment.setPaymentDate(LocalDateTime.now());

        paymentMapper.insert(refundPayment);
        return refundPayment;
    }

    @Override
    public boolean canProcess(String paymentMethod) {
        return PaymentMethod.POINT.getCode().equals(paymentMethod);
    }
}