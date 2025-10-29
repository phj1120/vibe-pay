package com.api.app.emum;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MEM002 implements CommonCode {
    EARN("001", "적립", 1, "", ""),
    USE("002", "사용", 2, "", ""),
    ;

    private final String code;
    private final String codeName;
    private final int displaySequence;
    private final String referenceValue1;
    private final String referenceValue2;

    public static MEM002 findByCode(String code) {
        return CommonCodeUtil.findByCode(MEM002.class, code);
    }
}
