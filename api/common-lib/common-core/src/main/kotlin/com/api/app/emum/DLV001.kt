package com.api.app.emum

/** 배송유형코드: deliveryTypeCode */
enum class DLV001(
    override val code: String,
    override val codeName: String,
    override val displaySequence: Int,
    override val referenceValue1: String,
    override val referenceValue2: String
) : CommonCode {
    DELIVERY("001", "일반배송", 1, "", ""),
    COLLECTION("002", "반품배송", 2, "", ""),
    ;

    companion object {
        fun findByCode(code: String): DLV001? = CommonCodeUtil.findByCode(DLV001::class.java, code)
    }
}
