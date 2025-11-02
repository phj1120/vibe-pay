package com.api.app.service.order;

import com.api.app.dto.request.order.OrderRequest;
import com.api.app.dto.response.order.OrderCompleteResponse;
import com.api.app.dto.response.order.OrderListResponse;

import java.util.List;

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

    /**
     * 주문 완료 정보 조회
     *
     * @param orderNo 주문번호
     * @param memberNo 회원번호
     * @return 주문 완료 정보
     */
    OrderCompleteResponse getOrderComplete(String orderNo, String memberNo);

    /**
     * 마이페이지 주문 목록 조회
     *
     * @param memberNo 회원번호
     * @return 주문 목록
     */
    List<OrderListResponse> getOrderList(String memberNo);
}
