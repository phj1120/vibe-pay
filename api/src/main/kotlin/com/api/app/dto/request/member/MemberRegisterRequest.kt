package com.api.app.dto.request.member

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class MemberRegisterRequest(
    @field:NotBlank(message = "회원명은 필수입니다")
    val memberName: String,

    @field:NotBlank(message = "전화번호는 필수입니다")
    val phone: String,

    @field:NotBlank(message = "이메일은 필수입니다")
    @field:Email(message = "이메일 형식이 올바르지 않습니다")
    val email: String,

    @field:NotBlank(message = "비밀번호는 필수입니다")
    val password: String
)
