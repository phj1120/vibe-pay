package com.api.app.emum

/** 상품상태코드: goodsStatusCode */
enum class PRD001(
    override val code: String,
    override val codeName: String,
    override val displaySequence: Int,
    override val referenceValue1: String,
    override val referenceValue2: String
) : CommonCode {
    ON_SALE("001", "판매중", 1, "", ""),
    DISCONTINUED("002", "판매중단", 2, "", ""),
    SOLD_OUT("003", "품절", 3, "", ""),
    ;

    companion object {
        fun findByCode(code: String): PRD001? = CommonCodeUtil.findByCode(PRD001::class.java, code)
    }
}
