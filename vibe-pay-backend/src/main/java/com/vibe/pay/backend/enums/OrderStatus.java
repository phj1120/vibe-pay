package com.vibe.pay.backend.enums;

public enum OrderStatus {
    ORDERED("주문완료"),
    CANCELLED("취소완료"),
    PAID("결제완료");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getCode() {
        return this.name();
    }
}