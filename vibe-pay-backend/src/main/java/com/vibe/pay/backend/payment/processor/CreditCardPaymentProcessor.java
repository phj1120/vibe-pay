package com.vibe.pay.backend.payment.processor;

import com.vibe.pay.backend.enums.PaymentMethod;
import com.vibe.pay.backend.enums.PayType;
import com.vibe.pay.backend.enums.PaymentStatus;
import com.vibe.pay.backend.payment.Payment;
import com.vibe.pay.backend.payment.PaymentConfirmRequest;
import com.vibe.pay.backend.payment.PaymentMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CreditCardPaymentProcessor implements PaymentProcessor {

    private static final Logger log = LoggerFactory.getLogger(CreditCardPaymentProcessor.class);

    @Autowired
    private PaymentMapper paymentMapper;

    @Override
    public Payment processPayment(PaymentConfirmRequest request) {
        log.info("Processing credit card payment for order: {}", request.getOrderId());

        // 카드 결제 로직
        Payment payment = new Payment();
        payment.setMemberId(request.getMemberId());
        payment.setOrderId(request.getOrderId());
        payment.setAmount(Double.valueOf(request.getPrice()));
        payment.setPaymentMethod(PaymentMethod.CREDIT_CARD.getCode());
        payment.setPayType(PayType.PAYMENT.getCode());
        payment.setPgCompany("INICIS"); // 현재는 이니시스만 지원
        payment.setStatus(PaymentStatus.SUCCESS.getCode());
        payment.setTransactionId(request.getAuthToken());
        payment.setPaymentDate(LocalDateTime.now());

        paymentMapper.insert(payment);
        return payment;
    }

    @Override
    public Payment processRefund(Payment originalPayment) {
        log.info("Processing credit card refund for payment: {}", originalPayment.getPaymentId());

        // 카드 환불 로직
        Payment refundPayment = new Payment();
        refundPayment.setMemberId(originalPayment.getMemberId());
        refundPayment.setOrderId(originalPayment.getOrderId());
        refundPayment.setAmount(originalPayment.getAmount());
        refundPayment.setPaymentMethod(PaymentMethod.CREDIT_CARD.getCode());
        refundPayment.setPayType(PayType.REFUND.getCode());
        refundPayment.setPgCompany(originalPayment.getPgCompany());
        refundPayment.setStatus(PaymentStatus.SUCCESS.getCode());
        refundPayment.setTransactionId(originalPayment.getPaymentId()); // 원본 결제 ID 참조
        refundPayment.setPaymentDate(LocalDateTime.now());

        paymentMapper.insert(refundPayment);
        return refundPayment;
    }

    @Override
    public boolean canProcess(String paymentMethod) {
        return PaymentMethod.CREDIT_CARD.getCode().equals(paymentMethod);
    }
}