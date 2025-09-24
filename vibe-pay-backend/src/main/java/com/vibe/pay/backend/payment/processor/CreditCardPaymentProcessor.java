package com.vibe.pay.backend.payment.processor;

import com.vibe.pay.backend.common.Constants;
import com.vibe.pay.backend.enums.*;
import com.vibe.pay.backend.exception.PaymentException;
import com.vibe.pay.backend.payment.Payment;
import com.vibe.pay.backend.payment.PaymentConfirmRequest;
import com.vibe.pay.backend.payment.PaymentMapper;
import com.vibe.pay.backend.payment.factory.PaymentGatewayFactory;
import com.vibe.pay.backend.payment.dto.PaymentCancelRequest;
import com.vibe.pay.backend.payment.dto.PaymentConfirmResponse;
import com.vibe.pay.backend.payment.gateway.PaymentGatewayAdapter;
import com.vibe.pay.backend.payment.dto.PaymentNetCancelRequest;
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

    @Autowired
    private PaymentGatewayFactory paymentGatewayFactory;

    @Override
    public Payment processPayment(PaymentConfirmRequest request) {
        log.info("Processing credit card payment for order: {}", request.getOrderId());

        try {
            // 1. PaymentId 생성
            String paymentId = generatePaymentId();
            request.setPaymentId(paymentId);
            log.info("Generated new paymentId: {}", paymentId);

            // 2. PG 어댑터를 사용하여 승인 처리
            PaymentGatewayAdapter pgAdapter = paymentGatewayFactory.getAdapter(PgCompany.INICIS.getCode());

            // PG 승인 처리
            PaymentConfirmResponse pgResponse = pgAdapter.confirm(request);
            if (!pgResponse.isSuccess()) {
                throw PaymentException.approvalFailed(pgResponse.getErrorMessage());
            }

            // 3. Payment 엔티티 생성 및 저장
            Payment payment = new Payment();
            payment.setPaymentId(paymentId);
            payment.setMemberId(request.getMemberId());
            payment.setOrderId(request.getOrderId());
            payment.setAmount(request.getPrice());
            payment.setPaymentMethod(PaymentMethod.CREDIT_CARD.getCode());
            payment.setPayType(PayType.PAYMENT.getCode());
            payment.setPgCompany(PgCompany.INICIS.getCode());
            payment.setStatus(PaymentStatus.SUCCESS.getCode());
            payment.setOrderStatus(OrderStatus.ORDERED.getCode());
            payment.setTransactionId(pgResponse.getTransactionId());
            payment.setPaymentDate(LocalDateTime.now());

            paymentMapper.insert(payment);

            log.info("Credit card payment processed successfully: paymentId={}, transactionId={}, amount={}",
                    paymentId, pgResponse.getTransactionId(), request.getPrice());

            return payment;
        } catch (PaymentException e) {
            log.error("Credit card payment failed: {}", e.getErrorMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during credit card payment processing: {}", e.getMessage(), e);
            throw PaymentException.pgSystemError("CREDIT_CARD_PAYMENT", e);
        }
    }

    @Override
    public Payment processRefund(Payment originalPayment) {
        String paymentId = generatePaymentId();

        log.info("Processing credit card refund for payment: {}", originalPayment.getPaymentId());

        // 카드 환불 로직
        Payment refundPayment = new Payment();
        refundPayment.setPaymentId(paymentId);
        refundPayment.setOrderId(originalPayment.getOrderId());
        refundPayment.setClaimId(originalPayment.getClaimId());
        refundPayment.setMemberId(originalPayment.getMemberId());
        refundPayment.setAmount(originalPayment.getAmount());
        refundPayment.setPaymentMethod(PaymentMethod.CREDIT_CARD.getCode());
        refundPayment.setPayType(PayType.REFUND.getCode());
        refundPayment.setPgCompany(originalPayment.getPgCompany());
        refundPayment.setStatus(PaymentStatus.SUCCESS.getCode());
        refundPayment.setOrderStatus(OrderStatus.CANCELLED.getCode());
        refundPayment.setTransactionId(originalPayment.getTransactionId()); // 원본 결제 ID 참조
        refundPayment.setPaymentDate(LocalDateTime.now());

        paymentMapper.insert(refundPayment);

        PaymentGatewayAdapter adapter = paymentGatewayFactory.getAdapter(refundPayment.getPgCompany());
        PaymentCancelRequest paymentCancelRequest = new PaymentCancelRequest();
        paymentCancelRequest.setPaymentId(paymentId);
        paymentCancelRequest.setAmount(refundPayment.getAmount());
        paymentCancelRequest.setTransactionId(refundPayment.getTransactionId());

        adapter.cancel(paymentCancelRequest);

        return refundPayment;
    }

    @Override
    public void netCancel(PaymentNetCancelRequest paymentNetCancelRequest) {
        PaymentGatewayAdapter adapter = paymentGatewayFactory.getAdapter(paymentNetCancelRequest.getPgCompany());
        adapter.netCancel(paymentNetCancelRequest);
    }

    @Override
    public boolean canProcess(String paymentMethod) {
        return PaymentMethod.CREDIT_CARD.getCode().equals(paymentMethod);
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