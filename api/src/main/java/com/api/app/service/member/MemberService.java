package com.api.app.service.member;

import com.api.app.dto.request.member.MemberLoginRequest;
import com.api.app.dto.request.member.MemberRegisterRequest;
import com.api.app.dto.response.member.MemberInfoResponse;
import com.api.app.dto.response.member.MemberLoginResponse;

/**
 * @author system
 * @version 1.0
 * @since 2025-10-28
 */
public interface MemberService {

    /**
     * 회원 가입
     *
     * @param request 회원 가입 요청
     */
    void registerMember(MemberRegisterRequest request);

    /**
     * 로그인
     *
     * @param request 로그인 요청
     * @return 로그인 응답 (액세스 토큰, 리프레시 토큰)
     */
    MemberLoginResponse login(MemberLoginRequest request);

    /**
     * 회원 정보 조회
     *
     * @param email 이메일
     * @return 회원 정보
     */
    MemberInfoResponse getMemberInfo(String email);
}
