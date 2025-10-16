package com.vibe.pay.domain.payment.service;

import com.vibe.pay.domain.payment.dto.PaymentConfirmRequest;
import com.vibe.pay.domain.payment.dto.PaymentMethodRequest;
import com.vibe.pay.domain.payment.entity.Payment;
import com.vibe.pay.domain.payment.factory.PaymentProcessorFactory;
import com.vibe.pay.domain.payment.processor.PaymentProcessor;
import com.vibe.pay.domain.payment.repository.PaymentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 결제 서비스
 * 결제 승인, 환불, 조회 등의 비즈니스 로직을 처리하는 계층
 *
 * Technical Specification 참조:
 * - docs/v5/TechnicalSpecification/payment-and-pg-integration-spec.md
 *
 * TODO: PG 연동 로직 구현 필요
 * - PG사별 결제 승인 API 연동 (이니시스, 나이스페이, 토스페이먼츠)
 * - PaymentGatewayFactory 및 PaymentProcessorFactory 활용
 * - PaymentGatewayAdapter를 통한 PG사별 연동 처리
 *
 * @see Payment
 * @see PaymentMapper
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentMapper paymentMapper;
    private final PaymentProcessorFactory processorFactory;

    /**
     * 결제 승인 처리
     *
     * PG사를 통한 결제 승인을 처리합니다.
     * PaymentProcessorFactory를 통해 결제 수단별 프로세서를 주입받아 처리합니다.
     *
     * Technical Specification 참조: payment-and-pg-integration-spec.md
     *
     * @param request 결제 수단 요청 DTO
     * @return 승인된 결제 엔티티
     * @throws RuntimeException 결제 승인 실패 시
     */
    @Transactional
    public Payment confirmPayment(PaymentMethodRequest request) {
        log.info("Confirming payment: method={}, amount={}",
                request.getPaymentMethod(), request.getAmount());

        try {
            // 1. PaymentProcessorFactory에서 결제 수단별 Processor 주입
            PaymentProcessor processor = processorFactory.getProcessor(request.getPaymentMethod());

            // 2. PaymentConfirmRequest 생성 (PaymentMethodRequest -> PaymentConfirmRequest 변환)
            PaymentConfirmRequest confirmRequest = new PaymentConfirmRequest();
            confirmRequest.setOrderId(request.getOrderId());
            confirmRequest.setMemberId(request.getMemberId());
            confirmRequest.setAmount(request.getAmount());
            confirmRequest.setPaymentMethod(request.getPaymentMethod());
            confirmRequest.setPgCompany(request.getPgCompany());

            // 3. PaymentProcessor.processPayment() 호출 - 내부적으로 PG사 연동 처리
            Payment payment = processor.processPayment(confirmRequest);

            log.info("Payment confirmed successfully: paymentId={}, transactionId={}",
                    payment.getPaymentId(), payment.getTransactionId());

            return payment;

        } catch (Exception e) {
            log.error("Payment confirmation failed: method={}, amount={}",
                    request.getPaymentMethod(), request.getAmount(), e);
            throw new RuntimeException("Payment confirmation failed: " + e.getMessage(), e);
        }
    }

    /**
     * 결제 환불 처리
     *
     * PG사를 통한 결제 환불을 처리합니다.
     * 현재는 stub 구현이며, 실제 PG 연동 시 구현 필요
     *
     * TODO: 실제 PG 연동 구현 필요 (payment-and-pg-integration-spec.md 참조)
     * 1. PaymentProcessor.processRefund(payment) 호출
     * 2. PaymentGatewayAdapter.cancel() 호출하여 PG사 취소 API 연동
     * 3. PaymentInterfaceRequestLogMapper에 요청/응답 로그 기록
     * 4. 환불 Payment 엔티티 생성 및 DB 저장 (payType=REFUND, 음수 금액)
     *
     * @param payment 환불할 원본 결제 엔티티
     * @throws RuntimeException 결제 환불 실패 시
     */
    @Transactional
    public void processRefund(Payment payment) {
        log.info("Processing refund: paymentId={}", payment.getPaymentId());

        // TODO: 실제 PG 환불 로직 구현
        // 1. PaymentProcessor.processRefund(payment)
        // 2. PaymentGatewayAdapter.cancel(PaymentCancelRequest) -> PG사 취소 API 호출
        // 3. 환불 Payment 엔티티 생성 및 저장

        log.info("Refund processed (stub): paymentId={}", payment.getPaymentId());
    }

    /**
     * 주문 ID로 결제 내역 조회
     *
     * @param orderId 주문 ID
     * @return 결제 엔티티 목록
     */
    public List<Payment> findByOrderId(String orderId) {
        log.debug("Fetching payments by orderId: {}", orderId);
        return paymentMapper.findByOrderId(orderId);
    }

    /**
     * 회원 ID로 결제 내역 조회
     *
     * @param memberId 회원 ID
     * @return 결제 엔티티 목록
     */
    public List<Payment> findByMemberId(Long memberId) {
        log.debug("Fetching payments by memberId: {}", memberId);
        return paymentMapper.findByMemberId(memberId);
    }
}
