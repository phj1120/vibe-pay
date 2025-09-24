package com.vibe.pay.backend.enums;

public enum PaymentStatus {
    SUCCESS("성공"),
    FAILED("실패"),
    CANCELLED("취소됨"),
    PENDING("처리중");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getCode() {
        return this.name();
    }
}