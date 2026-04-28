package com.api.app.emum

/** 포인트거래구분코드: pointTransactionCode */
enum class MEM002(
    override val code: String,
    override val codeName: String,
    override val displaySequence: Int,
    override val referenceValue1: String,
    override val referenceValue2: String
) : CommonCode {
    EARN("001", "적립", 1, "", ""),
    USE("002", "사용", 2, "", ""),
    ;

    companion object {
        fun findByCode(code: String): MEM002? = CommonCodeUtil.findByCode(MEM002::class.java, code)
    }
}
