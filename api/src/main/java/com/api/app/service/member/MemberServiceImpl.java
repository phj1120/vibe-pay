package com.api.app.service.member;

import com.api.app.common.exception.ApiError;
import com.api.app.common.exception.ApiException;
import com.api.app.common.jwt.JwtTokenProvider;
import com.api.app.dto.request.member.MemberLoginRequest;
import com.api.app.dto.request.member.MemberRegisterRequest;
import com.api.app.dto.request.member.TokenRefreshRequest;
import com.api.app.dto.response.member.MemberInfoResponse;
import com.api.app.dto.response.member.MemberLoginResponse;
import com.api.app.dto.response.member.TokenRefreshResponse;
import com.api.app.entity.MemberBase;
import com.api.app.repository.member.MemberBaseMapper;
import com.api.app.repository.member.MemberBaseTrxMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author system
 * @version 1.0
 * @since 2025-10-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberBaseMapper memberBaseMapper;
    private final MemberBaseTrxMapper memberBaseTrxMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public void registerMember(MemberRegisterRequest request) {
        log.debug("회원 가입 시작: {}", request.getEmail());

        // 이메일 중복 체크
        MemberBase existingMember = memberBaseMapper.selectMemberBaseByEmail(request.getEmail());
        if (existingMember != null) {
            throw new ApiException(ApiError.DUPLICATE_EMAIL);
        }

        // 회원번호 생성
        String memberNo = memberBaseTrxMapper.generateMemberNo();

        // Entity 생성
        MemberBase memberBase = new MemberBase();
        memberBase.setMemberNo(memberNo);
        memberBase.setMemberName(request.getMemberName());
        memberBase.setPhone(request.getPhone());
        memberBase.setEmail(request.getEmail());
        memberBase.setPassword(passwordEncoder.encode(request.getPassword()));
        memberBase.setMemberStatusCode("001"); // 정상회원

        // 회원 등록
        int result = memberBaseTrxMapper.insertMemberBase(memberBase);

        if (result != 1) {
            throw new ApiException(ApiError.INTERNAL_SERVER_ERROR, "회원 등록에 실패했습니다");
        }

        log.info("회원 가입 완료: memberNo={}, email={}", memberNo, request.getEmail());
    }

    @Override
    public MemberLoginResponse login(MemberLoginRequest request) {
        log.debug("로그인 시도: {}", request.getEmail());

        // 회원 조회
        MemberBase member = memberBaseMapper.selectMemberBaseByEmail(request.getEmail());
        if (member == null) {
            throw new ApiException(ApiError.INVALID_CREDENTIALS);
        }

        // 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new ApiException(ApiError.INVALID_CREDENTIALS);
        }

        // 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(member.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(member.getEmail());

        log.info("로그인 성공: email={}", request.getEmail());

        return new MemberLoginResponse(accessToken, refreshToken);
    }

    @Override
    public MemberInfoResponse getMemberInfo(String email) {
        log.debug("회원 정보 조회: {}", email);

        // 회원 조회
        MemberBase member = memberBaseMapper.selectMemberBaseByEmail(email);
        if (member == null) {
            throw new ApiException(ApiError.DATA_NOT_FOUND, "회원 정보를 찾을 수 없습니다");
        }

        // Response 변환
        MemberInfoResponse response = new MemberInfoResponse();
        response.setMemberName(member.getMemberName());
        response.setPhone(member.getPhone());
        response.setEmail(member.getEmail());
        response.setMemberStatusCode(member.getMemberStatusCode());

        return response;
    }

    @Override
    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
        log.debug("토큰 갱신 시도");

        try {
            // Refresh Token 검증
            String refreshToken = request.getRefreshToken();
            
            // 토큰 만료 여부 확인
            if (jwtTokenProvider.isTokenExpired(refreshToken)) {
                throw new ApiException(ApiError.EXPIRED_TOKEN, "리프레시 토큰이 만료되었습니다");
            }

            // 토큰에서 이메일 추출
            String email = jwtTokenProvider.extractEmail(refreshToken);

            // 회원 존재 확인
            MemberBase member = memberBaseMapper.selectMemberBaseByEmail(email);
            if (member == null) {
                throw new ApiException(ApiError.DATA_NOT_FOUND, "회원 정보를 찾을 수 없습니다");
            }

            // 새로운 토큰 생성
            String newAccessToken = jwtTokenProvider.generateAccessToken(email);
            String newRefreshToken = jwtTokenProvider.generateRefreshToken(email);

            log.info("토큰 갱신 성공: email={}", email);

            return new TokenRefreshResponse(newAccessToken, newRefreshToken);

        } catch (Exception e) {
            log.error("토큰 갱신 실패", e);
            throw new ApiException(ApiError.INVALID_TOKEN, "유효하지 않은 토큰입니다");
        }
    }
}
