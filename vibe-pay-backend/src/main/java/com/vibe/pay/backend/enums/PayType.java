package com.vibe.pay.backend.enums;

public enum PayType {
    PAYMENT("결제"),
    REFUND("환불");

    private final String description;

    PayType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getCode() {
        return this.name();
    }
}