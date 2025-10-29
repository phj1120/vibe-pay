package com.api.app.emum;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MEM001 implements CommonCode {
    ACTIVE("001", "정상회원", 1, "", ""),
    WITHDRAWN("002", "탈퇴회원", 2, "", ""),
    ;

    private final String code;
    private final String codeName;
    private final int displaySequence;
    private final String referenceValue1;
    private final String referenceValue2;

    public static MEM001 findByCode(String code) {
        return CommonCodeUtil.findByCode(MEM001.class, code);
    }
}
