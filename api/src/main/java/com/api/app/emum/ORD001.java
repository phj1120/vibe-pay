package com.api.app.emum;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ORD001 implements CommonCode {
    ORDER("001", "주문", 1, "", ""),
    ORDER_CANCEL("002", "주문취소", 2, "", ""),
    RETURN("101", "반품", 3, "", ""),
    RETURN_CANCEL("102", "반품취소", 4, "", ""),
    EXCHANGE("201", "교환", 5, "", ""),
    EXCHANGE_CANCEL("202", "교환취소", 6, "", ""),
    ;

    private final String code;
    private final String codeName;
    private final int displaySequence;
    private final String referenceValue1;
    private final String referenceValue2;

    public static ORD001 findByCode(String code) {
        return CommonCodeUtil.findByCode(ORD001.class, code);
    }
}
