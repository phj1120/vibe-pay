package com.vibepay.core.dto.internal

data class MemberValidateResponse(
    val memberNo: String,
    val email: String,
    val memberStatusCode: String,
    val memberName: String
)
