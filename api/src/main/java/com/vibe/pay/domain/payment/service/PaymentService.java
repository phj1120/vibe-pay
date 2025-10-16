package com.vibe.pay.domain.payment.service;

import com.vibe.pay.domain.payment.dto.PaymentCancelRequest;
import com.vibe.pay.domain.payment.dto.PaymentConfirmRequest;
import com.vibe.pay.domain.payment.dto.PaymentMethodRequest;
import com.vibe.pay.domain.payment.entity.Payment;
import com.vibe.pay.domain.payment.factory.PaymentGatewayFactory;
import com.vibe.pay.domain.payment.factory.PaymentProcessorFactory;
import com.vibe.pay.domain.payment.processor.PaymentProcessor;
import com.vibe.pay.domain.payment.repository.PaymentMapper;
import com.vibe.pay.domain.payment.adapter.PaymentGatewayAdapter;
import com.vibe.pay.enums.PayType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 결제 서비스
 * 결제 승인, 환불, 망취소 등의 비즈니스 로직을 처리하는 계층
 *
 * Technical Specification 참조:
 * - docs/v5/TechnicalSpecification/payment-and-pg-integration-spec.md
 *
 * 주요 기능:
 * - 결제 승인: PaymentProcessorFactory를 통한 결제 수단별 처리
 * - 결제 환불: PaymentProcessor.processRefund()를 통한 PG사 취소 API 연동
 * - 망취소: PaymentGatewayAdapter.netCancel()을 통한 PG사 망취소 API 연동
 *
 * @see Payment
 * @see PaymentMapper
 * @see PaymentProcessorFactory
 * @see PaymentGatewayFactory
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentMapper paymentMapper;
    private final PaymentProcessorFactory processorFactory;
    private final PaymentGatewayFactory gatewayFactory;

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
     * PaymentProcessorFactory를 통해 결제 수단별 프로세서를 주입받아 환불을 처리합니다.
     *
     * Technical Specification 참조: payment-and-pg-integration-spec.md
     *
     * @param payment 환불할 원본 결제 엔티티
     * @throws RuntimeException 결제 환불 실패 시
     */
    @Transactional
    public void processRefund(Payment payment) {
        log.info("Processing refund: paymentId={}, orderId={}, amount={}",
                payment.getPaymentId(), payment.getOrderId(), payment.getAmount());

        try {
            // 1. 환불 가능 여부 검증
            validateRefundable(payment);

            // 2. PaymentProcessorFactory에서 결제 수단별 Processor 주입
            PaymentProcessor processor = processorFactory.getProcessor(payment.getPaymentMethod());

            // 3. PaymentProcessor.processRefund() 호출 - 내부적으로 PG사 연동 처리
            // - PaymentGatewayAdapter.cancel() 호출하여 PG사 취소 API 연동
            // - PaymentInterfaceRequestLogMapper에 요청/응답 로그 기록
            // - 환불 Payment 엔티티 생성 및 DB 저장 (payType=REFUND, 음수 금액)
            processor.processRefund(payment);

            log.info("Refund processed successfully: paymentId={}", payment.getPaymentId());

        } catch (Exception e) {
            log.error("Refund processing failed: paymentId={}, orderId={}",
                    payment.getPaymentId(), payment.getOrderId(), e);
            throw new RuntimeException("Refund processing failed: " + e.getMessage(), e);
        }
    }

    /**
     * 환불 가능 여부 검증
     *
     * @param payment 원본 결제 엔티티
     * @throws RuntimeException 환불 불가능한 경우
     */
    private void validateRefundable(Payment payment) {
        // 1. 결제 상태 검증
        if (!"SUCCESS".equals(payment.getStatus())) {
            throw new RuntimeException("Only successful payments can be refunded: status=" + payment.getStatus());
        }

        // 2. 결제 타입 검증 (PAYMENT만 환불 가능)
        if (!PayType.PAYMENT.equals(payment.getPayType())) {
            throw new RuntimeException("Only payment type PAYMENT can be refunded: payType=" + payment.getPayType());
        }

        // 3. 이미 환불된 결제인지 확인
        List<Payment> refunds = paymentMapper.findByOrderId(payment.getOrderId());
        boolean alreadyRefunded = refunds.stream()
                .anyMatch(p -> PayType.REFUND.equals(p.getPayType())
                        && "SUCCESS".equals(p.getStatus()));

        if (alreadyRefunded) {
            throw new RuntimeException("Payment already refunded: paymentId=" + payment.getPaymentId());
        }

        log.debug("Payment validation passed for refund: paymentId={}", payment.getPaymentId());
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

    /**
     * 망취소 처리
     *
     * 결제 승인 후 주문 생성 실패 시 PG사에 이미 승인된 결제를 취소합니다.
     * 망취소는 결제 정보를 DB에 저장하지 않고, PG사 API만 호출합니다.
     *
     * Technical Specification 참조: payment-and-pg-integration-spec.md
     *
     * @param request 망취소 요청 (PaymentCancelRequest 사용)
     * @throws RuntimeException 망취소 실패 시
     */
    @Transactional
    public void netCancel(PaymentCancelRequest request) {
        log.info("Processing net cancel: paymentId={}, orderId={}, reason={}",
                request.getPaymentId(), request.getOrderId(), request.getReason());

        try {
            // 1. PaymentGatewayFactory에서 PG사별 Adapter 주입
            PaymentGatewayAdapter adapter = gatewayFactory.getAdapter(request.getPgCompany());

            // 2. PaymentGatewayAdapter.netCancel() 호출 - 내부적으로 PG사 망취소 API 연동
            // - PG사 망취소 API 호출
            // - PaymentInterfaceRequestLogMapper에 요청/응답 로그 기록
            adapter.netCancel(request);

            log.info("Net cancel processed successfully: paymentId={}, orderId={}",
                    request.getPaymentId(), request.getOrderId());

        } catch (Exception e) {
            log.error("Net cancel processing failed: paymentId={}, orderId={}, reason={}",
                    request.getPaymentId(), request.getOrderId(), request.getReason(), e);
            // 망취소 실패는 심각한 상황이므로 예외를 다시 던짐
            // 실무에서는 별도 알림/모니터링 시스템과 연동 필요
            throw new RuntimeException("Net cancel processing failed: " + e.getMessage(), e);
        }
    }
}
