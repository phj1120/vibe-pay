package com.api.app.controller.member

import com.api.app.common.response.ApiResponse
import com.api.app.dto.request.member.MemberLoginRequest
import com.api.app.dto.request.member.MemberRegisterRequest
import com.api.app.dto.request.member.TokenRefreshRequest
import com.api.app.dto.response.member.MemberInfoResponse
import com.api.app.dto.response.member.MemberLoginResponse
import com.api.app.dto.response.member.TokenRefreshResponse
import com.api.app.service.member.MemberService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/members")
class MemberController(private val memberService: MemberService) {

    private val log = LoggerFactory.getLogger(this::class.java)

    @PostMapping("/register")
    fun registerMember(@RequestBody @Valid request: MemberRegisterRequest): ApiResponse<Void> {
        log.info("회원 가입 요청: email={}", request.email)
        memberService.registerMember(request)
        return ApiResponse.success()
    }

    @PostMapping("/login")
    fun login(@RequestBody @Valid request: MemberLoginRequest): ApiResponse<MemberLoginResponse> {
        log.info("로그인 요청: email={}", request.email)
        return ApiResponse.success(memberService.login(request))
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    fun getMemberInfo(@AuthenticationPrincipal userDetails: UserDetails): ApiResponse<MemberInfoResponse> {
        return ApiResponse.success(memberService.getMemberInfo(userDetails.username))
    }

    @PostMapping("/refresh")
    fun refreshToken(@RequestBody @Valid request: TokenRefreshRequest): ApiResponse<TokenRefreshResponse> {
        return ApiResponse.success(memberService.refreshToken(request))
    }
}
