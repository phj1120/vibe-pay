package com.api.app.emum

/** 결제로그코드: payLogCode */
enum class PAY004(
    override val code: String,
    override val codeName: String,
    override val displaySequence: Int,
    override val referenceValue1: String,
    override val referenceValue2: String
) : CommonCode {
    PAYMENT("001", "결제", 1, "", ""),
    APPROVAL("002", "승인", 2, "", ""),
    NETWORK_CANCEL("003", "망취소", 3, "", ""),
    CANCEL("004", "취소", 4, "", ""),
    ;

    companion object {
        fun findByCode(code: String): PAY004? = CommonCodeUtil.findByCode(PAY004::class.java, code)
    }
}
