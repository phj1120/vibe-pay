package com.api.app.emum

/** 결제구분코드: payTypeCode */
enum class PAY001(
    override val code: String,
    override val codeName: String,
    override val displaySequence: Int,
    override val referenceValue1: String,
    override val referenceValue2: String
) : CommonCode {
    PAYMENT("001", "결제", 1, "", ""),
    REFUND("002", "환불", 2, "", ""),
    ;

    companion object {
        fun findByCode(code: String): PAY001? = CommonCodeUtil.findByCode(PAY001::class.java, code)
    }
}
