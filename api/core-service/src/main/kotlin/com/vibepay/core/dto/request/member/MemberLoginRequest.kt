package com.vibepay.core.dto.request.member

import jakarta.validation.constraints.NotBlank

data class MemberLoginRequest(
    @field:NotBlank val email: String,
    @field:NotBlank val password: String
)
