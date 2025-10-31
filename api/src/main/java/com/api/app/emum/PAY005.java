package com.api.app.emum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * PG 코드: pgTypeCode
 * */
@Getter
@AllArgsConstructor
public enum PAY005 implements CommonCode {
    INICIS("001", "이니시스", 1, "50", ""),
    NICE("002", "나이스", 2, "50", ""),
    ;

    private final String code;
    private final String codeName;
    private final int displaySequence;
    private final String referenceValue1;
    private final String referenceValue2;

    public static PAY005 findByCode(String code) {
        return CommonCodeUtil.findByCode(PAY005.class, code);
    }
}
