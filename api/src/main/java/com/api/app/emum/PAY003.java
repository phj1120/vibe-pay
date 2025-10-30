package com.api.app.emum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 결제상태코드: payStatusCode
 * */
@Getter
@AllArgsConstructor
public enum PAY003 implements CommonCode {
    PAYMENT_PENDING("001", "결제대기", 1, "", ""),
    PAYMENT_COMPLETED("002", "결제완료", 2, "", ""),
    PAYMENT_CANCELLED("003", "결제취소", 3, "", ""),
    REFUND_RECEIVED("101", "환불접수", 4, "", ""),
    REFUND_COMPLETED("102", "환불완료", 5, "", ""),
    REFUND_CANCELLED("103", "환불접수취소", 6, "", ""),
    ;

    private final String code;
    private final String codeName;
    private final int displaySequence;
    private final String referenceValue1;
    private final String referenceValue2;

    public static PAY003 findByCode(String code) {
        return CommonCodeUtil.findByCode(PAY003.class, code);
    }
}
