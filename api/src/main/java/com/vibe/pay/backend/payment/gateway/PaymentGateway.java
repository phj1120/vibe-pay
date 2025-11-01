package com.vibe.pay.backend.payment.gateway;

import com.vibe.pay.backend.payment.Payment;
import com.vibe.pay.backend.payment.dto.PaymentCancelRequest;
import com.vibe.pay.backend.payment.dto.PaymentConfirmRequest;
import com.vibe.pay.backend.payment.dto.PaymentInitRequest;
import com.vibe.pay.backend.payment.dto.PaymentInitResponse;
import com.vibe.pay.backend.payment.dto.PaymentNetCancelRequest;

/**
 * 결제 게이트웨이 통합 인터페이스 (Adapter Pattern)
 *
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
public interface PaymentGateway {

    /**
     * 결제 초기화
     *
     * @param request 결제 초기화 요청
     * @return 결제 초기화 응답
     */
    PaymentInitResponse initiate(PaymentInitRequest request);

    /**
     * 결제 승인
     *
     * @param request 결제 승인 요청
     * @return 승인된 결제 정보
     */
    Payment confirm(PaymentConfirmRequest request);

    /**
     * 결제 취소
     *
     * @param request 결제 취소 요청
     * @return 취소된 결제 정보
     */
    Payment cancel(PaymentCancelRequest request);

    /**
     * 결제 망취소
     *
     * @param request 망취소 요청
     * @return 망취소된 결제 정보
     */
    Payment netCancel(PaymentNetCancelRequest request);
}
