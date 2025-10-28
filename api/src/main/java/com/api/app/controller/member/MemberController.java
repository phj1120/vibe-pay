package com.api.app.controller.member;

import com.api.app.common.response.ApiResponse;
import com.api.app.dto.request.member.MemberLoginRequest;
import com.api.app.dto.request.member.MemberRegisterRequest;
import com.api.app.dto.response.member.MemberInfoResponse;
import com.api.app.dto.response.member.MemberLoginResponse;
import com.api.app.service.member.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * @author system
 * @version 1.0
 * @since 2025-10-28
 */
@Slf4j
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "회원 관리", description = "회원 가입, 로그인, 회원정보 조회 API")
public class MemberController {

    private final MemberService memberService;

    /**
     * 회원 가입
     *
     * @param request 회원 가입 요청
     * @return 성공 응답
     */
    @Operation(summary = "회원 가입", description = "신규 회원을 등록합니다")
    @PostMapping("/register")
    public ApiResponse<Void> registerMember(@RequestBody @Valid MemberRegisterRequest request) {
        log.info("회원 가입 요청: email={}", request.getEmail());
        memberService.registerMember(request);
        return ApiResponse.success();
    }

    /**
     * 로그인
     *
     * @param request 로그인 요청
     * @return 로그인 응답 (액세스 토큰, 리프레시 토큰)
     */
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다")
    @PostMapping("/login")
    public ApiResponse<MemberLoginResponse> login(@RequestBody @Valid MemberLoginRequest request) {
        log.info("로그인 요청: email={}", request.getEmail());
        MemberLoginResponse response = memberService.login(request);
        return ApiResponse.success(response);
    }

    /**
     * 회원 정보 조회
     *
     * @param userDetails 인증된 사용자 정보
     * @return 회원 정보
     */
    @Operation(summary = "회원 정보 조회", description = "현재 로그인한 회원의 정보를 조회합니다")
    @GetMapping("/me")
    public ApiResponse<MemberInfoResponse> getMemberInfo(
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("회원 정보 조회 요청: email={}", userDetails.getUsername());
        MemberInfoResponse response = memberService.getMemberInfo(userDetails.getUsername());
        return ApiResponse.success(response);
    }
}
