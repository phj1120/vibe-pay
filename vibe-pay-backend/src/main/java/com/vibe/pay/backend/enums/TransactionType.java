package com.vibe.pay.backend.enums;

public enum TransactionType {
    CHARGE("충전"),
    USE("사용"),
    REFUND("환불");

    private final String description;

    TransactionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getCode() {
        return this.name();
    }
}