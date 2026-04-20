package com.vibepay.core.controller

import com.api.app.common.response.ApiResponse
import com.vibepay.core.dto.request.member.MemberLoginRequest
import com.vibepay.core.dto.request.member.MemberRegisterRequest
import com.vibepay.core.dto.request.member.TokenRefreshRequest
import com.vibepay.core.dto.response.member.MemberInfoResponse
import com.vibepay.core.dto.response.member.MemberLoginResponse
import com.vibepay.core.dto.response.member.TokenRefreshResponse
import com.vibepay.core.service.MemberService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/members")
class MemberController(private val memberService: MemberService) {

    @PostMapping("/register")
    fun register(@RequestBody @Valid request: MemberRegisterRequest): ApiResponse<Void> {
        memberService.registerMember(request)
        return ApiResponse.success()
    }

    @PostMapping("/login")
    fun login(@RequestBody @Valid request: MemberLoginRequest): ApiResponse<MemberLoginResponse> =
        ApiResponse.success(memberService.login(request))

    @GetMapping("/me")
    fun getMemberInfo(@RequestHeader("X-User-Email") email: String): ApiResponse<MemberInfoResponse> =
        ApiResponse.success(memberService.getMemberInfo(email))

    @PostMapping("/refresh")
    fun refreshToken(@RequestBody @Valid request: TokenRefreshRequest): ApiResponse<TokenRefreshResponse> =
        ApiResponse.success(memberService.refreshToken(request))
}
