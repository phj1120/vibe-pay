package com.vibe.pay.enums;

/**
 * PG사 종류를 나타내는 Enum
 *
 * 지원되는 PG사:
 * - INICIS: KG 이니시스
 * - NICEPAY: 나이스페이
 * - TOSS: 토스페이먼츠
 */
public enum PgCompany {
    INICIS("KG이니시스"),
    NICEPAY("나이스페이"),
    TOSS("토스페이먼츠");

    private final String description;

    PgCompany(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
