package com.api.app.emum

/** 주문유형코드: orderTypeCode */
enum class ORD001(
    override val code: String,
    override val codeName: String,
    override val displaySequence: Int,
    override val referenceValue1: String,
    override val referenceValue2: String
) : CommonCode {
    ORDER("001", "주문", 1, "", ""),
    ORDER_CANCEL("002", "주문취소", 2, "", ""),
    RETURN("101", "반품", 3, "", ""),
    RETURN_CANCEL("102", "반품취소", 4, "", ""),
    EXCHANGE("201", "교환", 5, "", ""),
    EXCHANGE_CANCEL("202", "교환취소", 6, "", ""),
    ;

    companion object {
        fun findByCode(code: String): ORD001? = CommonCodeUtil.findByCode(ORD001::class.java, code)
    }
}
