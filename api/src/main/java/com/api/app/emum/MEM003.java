package com.api.app.emum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 포인트적립사용사유코드: pointTransactionReasonCode
 * */
@Getter
@AllArgsConstructor
public enum MEM003 implements CommonCode {
    ETC("001", "기타", 1, "365", ""),
    ORDER("002", "주문", 2, "365", ""),
    ;

    private final String code;
    private final String codeName;
    private final int displaySequence;
    private final String referenceValue1;
    private final String referenceValue2;

    public static MEM003 findByCode(String code) {
        return CommonCodeUtil.findByCode(MEM003.class, code);
    }
}
