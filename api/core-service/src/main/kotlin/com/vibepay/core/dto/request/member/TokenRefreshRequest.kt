package com.vibepay.core.dto.request.member

import jakarta.validation.constraints.NotBlank

data class TokenRefreshRequest(@field:NotBlank val refreshToken: String)
