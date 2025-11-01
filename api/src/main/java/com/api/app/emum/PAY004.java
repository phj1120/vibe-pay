package com.api.app.emum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 로그 유형 코드: payLogCode
 * */
@Getter
@AllArgsConstructor
public enum PAY004 implements CommonCode {
    PAYMENT("001", "결제", 1, "", ""),
    APPROVAL("002", "승인", 2, "", ""),
    NETWORK_CANCEL("003", "망취소", 3, "", ""),
    CANCEL("004", "취소", 3, "", ""),
    ;

    private final String code;
    private final String codeName;
    private final int displaySequence;
    private final String referenceValue1;
    private final String referenceValue2;

    public static PAY004 findByCode(String code) {
        return CommonCodeUtil.findByCode(PAY004.class, code);
    }
}
