package com.api.app.emum;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DLV001 implements CommonCode {
    SHIPMENT("001", "출하", 1, "", ""),
    COLLECTION("002", "회수", 2, "", ""),
    ;

    private final String code;
    private final String codeName;
    private final int displaySequence;
    private final String referenceValue1;
    private final String referenceValue2;

    public static DLV001 findByCode(String code) {
        return CommonCodeUtil.findByCode(DLV001.class, code);
    }
}
