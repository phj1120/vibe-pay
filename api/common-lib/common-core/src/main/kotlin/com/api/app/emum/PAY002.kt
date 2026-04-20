package com.api.app.emum

/** 결제방식코드: payWayCode */
enum class PAY002(
    override val code: String,
    override val codeName: String,
    override val displaySequence: Int,
    override val referenceValue1: String,
    override val referenceValue2: String
) : CommonCode {
    CREDIT_CARD("001", "신용카드", 1, "", ""),
    POINT("002", "포인트", 2, "", ""),
    ;

    companion object {
        fun findByCode(code: String): PAY002? = CommonCodeUtil.findByCode(PAY002::class.java, code)
    }
}
