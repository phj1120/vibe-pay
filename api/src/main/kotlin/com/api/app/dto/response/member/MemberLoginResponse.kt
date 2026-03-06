package com.api.app.dto.response.member

data class MemberLoginResponse(
    val accessToken: String,
    val refreshToken: String
)
