package com.api.app.emum;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ORD002 implements CommonCode {
    ORDER_RECEIVED("001", "주문접수", 1, "", ""),
    ORDER_COMPLETED("002", "주문완료", 2, "", ""),
    ORDER_CANCELLED("003", "주문취소", 3, "", ""),
    DELIVERY_COMPLETED("107", "배송완료", 4, "", ""),
    RETURN_COMPLETED("207", "반품완료", 5, "", ""),
    ;

    private final String code;
    private final String codeName;
    private final int displaySequence;
    private final String referenceValue1;
    private final String referenceValue2;

    public static ORD002 findByCode(String code) {
        return CommonCodeUtil.findByCode(ORD002.class, code);
    }
}
