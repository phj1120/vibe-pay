package com.api.app.emum

/** PG사코드: pgTypeCode */
enum class PAY005(
    override val code: String,
    override val codeName: String,
    override val displaySequence: Int,
    override val referenceValue1: String,
    override val referenceValue2: String
) : CommonCode {
    INICIS("001", "이니시스", 1, "0", ""),
    NICE("002", "나이스", 2, "0", ""),
    TEST("999", "테스트PG", 999, "100", ""),
    ;

    fun getReferenceValue1AsInt(): Int = referenceValue1.toInt()

    companion object {
        fun findByCode(code: String): PAY005? = CommonCodeUtil.findByCode(PAY005::class.java, code)
    }
}
