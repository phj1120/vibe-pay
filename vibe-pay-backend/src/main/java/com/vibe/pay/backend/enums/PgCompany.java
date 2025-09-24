package com.vibe.pay.backend.enums;

public enum PgCompany {
    INICIS("이니시스"),
    NICEPAY("나이스페이"),
    TOSS("토스페이먼츠");

    private final String description;

    PgCompany(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getCode() {
        return this.name();
    }
}