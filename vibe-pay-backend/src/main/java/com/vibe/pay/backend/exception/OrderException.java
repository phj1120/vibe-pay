package com.vibe.pay.backend.exception;

public class OrderException extends BusinessException {

    public OrderException(String errorMessage) {
        super("ORDER_ERROR", errorMessage);
    }

    public OrderException(String errorMessage, Throwable cause) {
        super("ORDER_ERROR", errorMessage, cause);
    }

    // 특정 주문 에러들
    public static OrderException orderNotFound(String orderId) {
        return new OrderException("주문을 찾을 수 없습니다: " + orderId);
    }

    public static OrderException alreadyCancelled(String orderId) {
        return new OrderException("이미 취소된 주문입니다: " + orderId);
    }

    public static OrderException productNotFound(Long productId) {
        return new OrderException("상품을 찾을 수 없습니다: " + productId);
    }

    public static OrderException invalidOrderAmount() {
        return new OrderException("잘못된 주문 금액입니다");
    }

    public static OrderException creationFailed(String message) {
        return new OrderException("주문 생성 실패: " + message);
    }

    public static OrderException paymentFailed(String message) {
        return new OrderException("결제 처리 실패: " + message);
    }
}