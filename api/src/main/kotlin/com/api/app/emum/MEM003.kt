package com.api.app.emum

/** 포인트거래사유코드: pointTransactionReasonCode */
enum class MEM003(
    override val code: String,
    override val codeName: String,
    override val displaySequence: Int,
    override val referenceValue1: String,
    override val referenceValue2: String
) : CommonCode {
    PURCHASE_EARN("001", "구매적립", 1, "365", ""),
    ORDER("002", "구매사용", 2, "365", ""),
    CANCEL("003", "취소", 3, "365", ""),
    ETC("004", "기타", 4, "365", ""),
    ;

    companion object {
        fun findByCode(code: String): MEM003? = CommonCodeUtil.findByCode(MEM003::class.java, code)
    }
}
