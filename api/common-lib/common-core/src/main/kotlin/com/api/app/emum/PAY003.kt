package com.api.app.emum

/** 결제상태코드: payStatusCode */
enum class PAY003(
    override val code: String,
    override val codeName: String,
    override val displaySequence: Int,
    override val referenceValue1: String,
    override val referenceValue2: String
) : CommonCode {
    PAYMENT_PENDING("001", "결제대기", 1, "", ""),
    PAYMENT_COMPLETED("002", "결제완료", 2, "", ""),
    PAYMENT_CANCELLED("003", "결제취소", 3, "", ""),
    ;

    companion object {
        fun findByCode(code: String): PAY003? = CommonCodeUtil.findByCode(PAY003::class.java, code)
    }
}
