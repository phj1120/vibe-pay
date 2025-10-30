package com.api.app.emum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 상품상태코드: goodsStatusCode
 * */
@Getter
@AllArgsConstructor
public enum PRD001 implements CommonCode {
    ON_SALE("001", "판매중", 1, "", ""),
    SALE_STOPPED("002", "판매중단", 2, "", ""),
    SALE_SUSPENDED("003", "판매중지", 3, "", ""),
    ;

    private final String code;
    private final String codeName;
    private final int displaySequence;
    private final String referenceValue1;
    private final String referenceValue2;

    public static PRD001 findByCode(String code) {
        return CommonCodeUtil.findByCode(PRD001.class, code);
    }
}
