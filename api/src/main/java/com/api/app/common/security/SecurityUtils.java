package com.api.app.common.security;

import com.api.app.common.exception.ApiError;
import com.api.app.common.exception.ApiException;
import com.api.app.entity.MemberBase;
import com.api.app.repository.member.MemberBaseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Spring Security Context에서 인증된 사용자 정보를 조회하는 유틸리티
 *
 * @author Claude
 * @version 1.0
 * @since 2025-10-31
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final MemberBaseMapper memberBaseMapper;

    /**
     * 현재 인증된 사용자의 이메일을 반환
     *
     * @return 사용자 이메일
     * @throws ApiException 인증되지 않은 경우
     */
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("Authentication not found in SecurityContext");
            throw new ApiException(ApiError.UNAUTHORIZED, "인증되지 않은 사용자입니다");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            String email = ((UserDetails) principal).getUsername();
            log.debug("Current user email: {}", email);
            return email;
        }

        log.error("Invalid principal type: {}", principal.getClass().getName());
        throw new ApiException(ApiError.UNAUTHORIZED, "인증 정보가 올바르지 않습니다");
    }

    /**
     * 현재 인증된 사용자의 회원번호를 반환
     *
     * @return 회원번호
     * @throws ApiException 인증되지 않았거나 회원 정보를 찾을 수 없는 경우
     */
    public String getCurrentUserMemberNo() {
        String email = getCurrentUserEmail();

        MemberBase member = memberBaseMapper.selectMemberBaseByEmail(email);

        if (member == null) {
            log.error("Member not found for email: {}", email);
            throw new ApiException(ApiError.DATA_NOT_FOUND, "회원 정보를 찾을 수 없습니다");
        }

        log.debug("Current user memberNo: {}", member.getMemberNo());
        return member.getMemberNo();
    }

    /**
     * 현재 인증된 사용자의 회원 정보를 반환
     *
     * @return 회원 정보
     * @throws ApiException 인증되지 않았거나 회원 정보를 찾을 수 없는 경우
     */
    public MemberBase getCurrentUser() {
        String email = getCurrentUserEmail();

        MemberBase member = memberBaseMapper.selectMemberBaseByEmail(email);

        if (member == null) {
            log.error("Member not found for email: {}", email);
            throw new ApiException(ApiError.DATA_NOT_FOUND, "회원 정보를 찾을 수 없습니다");
        }

        return member;
    }
}
