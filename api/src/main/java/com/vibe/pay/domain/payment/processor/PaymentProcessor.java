package com.vibe.pay.domain.payment.processor;

import com.vibe.pay.domain.payment.dto.PaymentConfirmRequest;
import com.vibe.pay.domain.payment.entity.Payment;
import com.vibe.pay.enums.PaymentMethod;

/**
 * 결제 수단별 처리를 위한 프로세서 인터페이스
 *
 * 결제 수단(신용카드, 포인트 등)별로 구현체를 제공하여
 * 결제 및 환불 처리를 담당합니다.
 *
 * Technical Specification 참조:
 * - docs/v5/TechnicalSpecification/payment-and-pg-integration-spec.md
 *
 * @see CreditCardPaymentProcessor
 * @see PointPaymentProcessor
 */
public interface PaymentProcessor {

    /**
     * 해당 결제 수단을 지원하는지 확인
     *
     * @param paymentMethod 결제 수단 enum
     * @return 지원 여부
     */
    boolean supports(PaymentMethod paymentMethod);

    /**
     * 결제 처리
     *
     * @param request 결제 승인 요청
     * @return 생성된 Payment 엔티티
     * @throws RuntimeException 결제 처리 실패 시
     */
    Payment processPayment(PaymentConfirmRequest request);

    /**
     * 환불 처리
     *
     * @param payment 환불할 원본 결제
     * @throws RuntimeException 환불 처리 실패 시
     */
    void processRefund(Payment payment);
}