package com.api.app.emum

/** 주문상태코드: orderStatusCode */
enum class ORD002(
    override val code: String,
    override val codeName: String,
    override val displaySequence: Int,
    override val referenceValue1: String,
    override val referenceValue2: String
) : CommonCode {
    ORDER_RECEIVED("001", "주문접수", 1, "", ""),
    ORDER_COMPLETED("002", "주문완료", 2, "", ""),
    ORDER_CANCELLED("003", "주문취소", 3, "", ""),
    DELIVERY_COMPLETED("107", "배송완료", 7, "", ""),
    RETURN_COMPLETED("207", "반품완료", 8, "", ""),
    ;

    companion object {
        fun findByCode(code: String): ORD002? = CommonCodeUtil.findByCode(ORD002::class.java, code)
    }
}
