package com.api.app.emum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 결제방식코드: payWayCode
 * */
@Getter
@AllArgsConstructor
public enum PAY002 implements CommonCode {
    CREDIT_CARD("001", "신용카드", 1, "", ""),
    POINT("002", "포인트", 2, "", ""),
    ;

    private final String code;
    private final String codeName;
    private final int displaySequence;
    private final String referenceValue1;
    private final String referenceValue2;

    public static PAY002 findByCode(String code) {
        return CommonCodeUtil.findByCode(PAY002.class, code);
    }
}
