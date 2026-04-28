package com.api.app.service.member

import com.api.app.dto.request.member.MemberLoginRequest
import com.api.app.dto.request.member.MemberRegisterRequest
import com.api.app.dto.request.member.TokenRefreshRequest
import com.api.app.dto.response.member.MemberInfoResponse
import com.api.app.dto.response.member.MemberLoginResponse
import com.api.app.dto.response.member.TokenRefreshResponse

interface MemberService {
    fun registerMember(request: MemberRegisterRequest)
    fun login(request: MemberLoginRequest): MemberLoginResponse
    fun getMemberInfo(email: String): MemberInfoResponse
    fun refreshToken(request: TokenRefreshRequest): TokenRefreshResponse
}
