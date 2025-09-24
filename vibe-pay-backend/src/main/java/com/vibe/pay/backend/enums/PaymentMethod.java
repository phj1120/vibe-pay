package com.vibe.pay.backend.enums;

public enum PaymentMethod {
    CREDIT_CARD("신용카드"),
    POINT("포인트");

    private final String description;

    PaymentMethod(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getCode() {
        return this.name();
    }
}