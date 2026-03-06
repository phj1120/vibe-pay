package com.api.app.dto.response.member

data class TokenRefreshResponse(
    val accessToken: String,
    val refreshToken: String
)
