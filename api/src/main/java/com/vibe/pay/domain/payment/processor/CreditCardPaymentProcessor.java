package com.vibe.pay.domain.payment.processor;

import com.vibe.pay.domain.payment.dto.PaymentCancelRequest;
import com.vibe.pay.domain.payment.dto.PaymentCancelResponse;
import com.vibe.pay.domain.payment.dto.PaymentConfirmRequest;
import com.vibe.pay.domain.payment.dto.PaymentConfirmResponse;
import com.vibe.pay.domain.payment.entity.Payment;
import com.vibe.pay.domain.payment.factory.PaymentGatewayFactory;
import com.vibe.pay.domain.payment.repository.PaymentMapper;
import com.vibe.pay.enums.PaymentMethod;
import com.vibe.pay.enums.PayType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 신용카드 결제 프로세서
 *
 * 신용카드 결제 수단의 결제 및 환불 처리를 담당합니다.
 * PG사 연동을 통해 실제 결제 승인/취소를 처리합니다.
 *
 * Technical Specification 참조:
 * - docs/v5/TechnicalSpecification/payment-and-pg-integration-spec.md
 *
 * @see PaymentProcessor
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CreditCardPaymentProcessor implements PaymentProcessor {

    private final PaymentMapper paymentMapper;
    private final PaymentGatewayFactory gatewayFactory;

    @Override
    public boolean supports(PaymentMethod paymentMethod) {
        return PaymentMethod.CREDIT_CARD.equals(paymentMethod);
    }

    @Override
    public Payment processPayment(PaymentConfirmRequest request) {
        log.info("Processing credit card payment: orderId={}, amount={}",
                request.getOrderId(), request.getAmount());

        try {
            // 1. Payment 시퀀스 생성
            Long paymentSequence = paymentMapper.getNextPaymentSequence();

            // 2. PaymentGatewayAdapter를 통한 PG사 승인 처리
            PaymentConfirmResponse pgResponse = gatewayFactory
                    .getAdapter(request.getPgCompany())
                    .confirm(request);

            if (!pgResponse.isSuccess()) {
                throw new RuntimeException("PG payment confirmation failed: " + pgResponse.getMessage());
            }

            // 3. Payment 엔티티 생성
            Payment payment = new Payment();
            payment.setPaymentId(generatePaymentId(paymentSequence));
            payment.setOrderId(request.getOrderId());
            payment.setMemberId(request.getMemberId());
            payment.setAmount(request.getAmount());
            payment.setPaymentMethod(PaymentMethod.CREDIT_CARD);
            payment.setPayType(PayType.PAYMENT);
            payment.setPgCompany(request.getPgCompany());
            payment.setTransactionId(pgResponse.getTransactionId());
            payment.setApprovalNumber(pgResponse.getApprovalNumber());
            payment.setPaymentDate(LocalDateTime.now());
            payment.setStatus("SUCCESS");

            // 4. DB 저장
            paymentMapper.insert(payment);

            log.info("Credit card payment processed successfully: paymentId={}, transactionId={}",
                    payment.getPaymentId(), payment.getTransactionId());

            return payment;

        } catch (Exception e) {
            log.error("Credit card payment processing failed: orderId={}", request.getOrderId(), e);
            throw new RuntimeException("Credit card payment processing failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void processRefund(Payment payment) {
        log.info("Processing credit card refund: paymentId={}, orderId={}, amount={}",
                payment.getPaymentId(), payment.getOrderId(), payment.getAmount());

        try {
            // 1. PaymentCancelRequest 생성
            PaymentCancelRequest cancelRequest = new PaymentCancelRequest();
            cancelRequest.setOrderId(payment.getOrderId());
            cancelRequest.setPaymentId(payment.getPaymentId());
            cancelRequest.setAmount(payment.getAmount());
            cancelRequest.setPgCompany(payment.getPgCompany());
            cancelRequest.setOriginalTransactionId(payment.getTransactionId());
            cancelRequest.setOriginalApprovalNumber(payment.getApprovalNumber());
            cancelRequest.setCancelReason("User requested refund");
            cancelRequest.setClaimId(payment.getClaimId());

            // 2. PaymentGatewayAdapter.cancel() 호출하여 PG사 취소 API 연동
            PaymentCancelResponse cancelResponse = gatewayFactory
                    .getAdapter(payment.getPgCompany())
                    .cancel(cancelRequest);

            if (!cancelResponse.isSuccess()) {
                throw new RuntimeException("PG refund failed: " + cancelResponse.getMessage());
            }

            // 3. 환불 Payment 엔티티 생성 (payType=REFUND, 음수 금액)
            Long refundSequence = paymentMapper.getNextPaymentSequence();

            Payment refundPayment = new Payment();
            refundPayment.setPaymentId(generatePaymentId(refundSequence));
            refundPayment.setOrderId(payment.getOrderId());
            refundPayment.setMemberId(payment.getMemberId());
            refundPayment.setAmount(payment.getAmount().negate()); // 음수 금액
            refundPayment.setPaymentMethod(PaymentMethod.CREDIT_CARD);
            refundPayment.setPayType(PayType.REFUND);
            refundPayment.setPgCompany(payment.getPgCompany());
            refundPayment.setTransactionId(cancelResponse.getCancelTransactionId());
            refundPayment.setApprovalNumber(cancelResponse.getCancelApprovalNumber());
            refundPayment.setPaymentDate(LocalDateTime.now());
            refundPayment.setStatus("SUCCESS");
            refundPayment.setClaimId(payment.getClaimId());

            // 4. DB 저장
            paymentMapper.insert(refundPayment);

            log.info("Credit card refund processed successfully: originalPaymentId={}, refundPaymentId={}, refundTransactionId={}",
                    payment.getPaymentId(), refundPayment.getPaymentId(), refundPayment.getTransactionId());

        } catch (Exception e) {
            log.error("Credit card refund processing failed: paymentId={}", payment.getPaymentId(), e);
            throw new RuntimeException("Credit card refund processing failed: " + e.getMessage(), e);
        }
    }

    /**
     * 결제 ID 생성
     * 형식: PAY + YYYYMMDD + 10자리 시퀀스
     */
    private String generatePaymentId(Long sequence) {
        String dateStr = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sequenceStr = String.format("%010d", sequence);
        return "PAY" + dateStr + sequenceStr;
    }
}