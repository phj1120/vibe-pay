package com.api.app.service.payment.method;

import com.api.app.dto.request.order.PayRequest;
import com.api.app.entity.PayBase;

/**
 * 결제 방식 전략 인터페이스 (카드/포인트)
 *
 * @author Claude
 * @version 1.0
 * @since 2025-10-31
 */
public interface PaymentMethodStrategy {

    /**
     * 결제 처리
     *
     * @param memberNo 회원번호
     * @param orderNo 주문번호
     * @param payRequest 결제 요청
     * @return 결제 정보 Entity
     */
    PayBase processPayment(String memberNo, String orderNo, PayRequest payRequest);

    /**
     * 결제 방식 코드 반환
     *
     * @return 결제 방식 코드 (PAY002)
     */
    String getPayWayCode();
}
