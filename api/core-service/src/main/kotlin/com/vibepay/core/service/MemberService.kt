package com.vibepay.core.service

import com.vibepay.core.dto.request.member.MemberLoginRequest
import com.vibepay.core.dto.request.member.MemberRegisterRequest
import com.vibepay.core.dto.request.member.TokenRefreshRequest
import com.vibepay.core.dto.response.member.MemberInfoResponse
import com.vibepay.core.dto.response.member.MemberLoginResponse
import com.vibepay.core.dto.response.member.TokenRefreshResponse
import com.vibepay.core.entity.MemberBase

interface MemberService {
    fun registerMember(request: MemberRegisterRequest)
    fun login(request: MemberLoginRequest): MemberLoginResponse
    fun getMemberInfo(email: String): MemberInfoResponse
    fun refreshToken(request: TokenRefreshRequest): TokenRefreshResponse
    fun findByEmail(email: String): MemberBase?
    fun findByMemberNo(memberNo: String): MemberBase?
}
