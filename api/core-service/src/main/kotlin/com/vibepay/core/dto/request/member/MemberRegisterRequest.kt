package com.vibepay.core.dto.request.member

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class MemberRegisterRequest(
    @field:NotBlank val memberName: String,
    @field:NotBlank val phone: String,
    @field:NotBlank @field:Email val email: String,
    @field:NotBlank val password: String
)
