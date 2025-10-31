package com.api.app.service.payment;

import com.api.app.dto.request.payment.PaymentInitiateRequest;
import com.api.app.dto.response.payment.PaymentInitiateResponse;

/**
 * 결제 서비스 인터페이스
 *
 * @author Claude
 * @version 1.0
 * @since 2025-10-31
 */
public interface PaymentService {

    /**
     * 결제 초기화
     *
     * @param request 결제 초기화 요청
     * @return 결제 초기화 응답
     */
    PaymentInitiateResponse initiatePayment(PaymentInitiateRequest request);
}
