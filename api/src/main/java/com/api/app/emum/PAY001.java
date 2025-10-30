package com.api.app.emum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 결제유형코드: payTypeCode
 * */
@Getter
@AllArgsConstructor
public enum PAY001 implements CommonCode {
    PAYMENT("001", "결제", 1, "", ""),
    REFUND("002", "환불", 2, "", ""),
    ;

    private final String code;
    private final String codeName;
    private final int displaySequence;
    private final String referenceValue1;
    private final String referenceValue2;

    public static PAY001 findByCode(String code) {
        return CommonCodeUtil.findByCode(PAY001.class, code);
    }
}
