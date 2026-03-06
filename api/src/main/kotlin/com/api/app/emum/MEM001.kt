package com.api.app.emum

/** 회원상태코드: memberStatusCode */
enum class MEM001(
    override val code: String,
    override val codeName: String,
    override val displaySequence: Int,
    override val referenceValue1: String,
    override val referenceValue2: String
) : CommonCode {
    ACTIVE("001", "정상회원", 1, "", ""),
    WITHDRAWN("002", "탈퇴회원", 2, "", ""),
    ;

    companion object {
        fun findByCode(code: String): MEM001? = CommonCodeUtil.findByCode(MEM001::class.java, code)
    }
}
