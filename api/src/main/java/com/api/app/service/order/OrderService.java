package com.api.app.service.order;

import com.api.app.dto.request.order.OrderRequest;

/**
 * 주문 서비스 인터페이스
 *
 * @author Claude
 * @version 1.0
 * @since 2025-10-31
 */
public interface OrderService {

    /**
     * 주문번호 생성
     *
     * @return 생성된 주문번호
     */
    String generateOrderNumber();

    /**
     * 주문 생성
     *
     * @param request 주문 요청
     */
    void createOrder(OrderRequest request);
}
