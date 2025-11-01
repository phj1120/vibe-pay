package com.vibe.pay.backend.payment.processor;

import com.vibe.pay.backend.payment.Payment;
import com.vibe.pay.backend.payment.dto.PaymentConfirmRequest;
import com.vibe.pay.backend.payment.dto.PaymentNetCancelRequest;

/**
 * 결제 처리 전략 인터페이스 (Strategy Pattern)
 *
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
public interface PaymentProcessor {

    /**
     * 결제 처리
     *
     * @param request 결제 승인 요청
     * @return 처리된 결제 정보
     */
    Payment processPayment(PaymentConfirmRequest request);

    /**
     * 결제 환불 처리
     *
     * @param payment 환불할 결제 정보
     */
    void processRefund(Payment payment);

    /**
     * 결제 망취소 처리
     *
     * @param request 망취소 요청
     */
    void netCancel(PaymentNetCancelRequest request);
}
