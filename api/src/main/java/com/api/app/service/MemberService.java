package com.api.app.service;

import com.api.app.common.security.jwt.JwtTokenProvider;
import com.api.app.entity.memberbase.MemberBaseDto;
import com.api.app.mapper.memberbase.MemberBaseMapper;
import com.api.app.member.request.LoginRequestDto;
import com.api.app.member.request.SignupRequestDto;
import com.api.app.member.response.TokenResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberBaseMapper memberBaseMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public void signup(SignupRequestDto request) {
        if (memberBaseMapper.findByEmail(request.getEmail()) != null) {
            throw new IllegalArgumentException("Email already exists.");
        }

        MemberBaseDto member = new MemberBaseDto();
        member.setMemberNo(UUID.randomUUID().toString()); // Simple UUID generation
        member.setMemberName(request.getMemberName());
        member.setPhone(request.getPhone());
        member.setEmail(request.getEmail());
        member.setPassword(passwordEncoder.encode(request.getPassword()));
        member.setMemberStatusCode("001"); // 정상회원
        member.setRegistId("SYSTEM");
        member.setRegistDateTime(LocalDateTime.now());
        member.setModifyId("SYSTEM");
        member.setModifyDateTime(LocalDateTime.now());

        memberBaseMapper.insertMemberBase(member);
    }

    @Transactional
    public TokenResponseDto login(LoginRequestDto request) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        String accessToken = jwtTokenProvider.createAccessToken(authentication);
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

        return new TokenResponseDto(accessToken, refreshToken);
    }

    @Transactional
    public TokenResponseDto refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid Refresh Token.");
        }

        Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);

        // Generate new tokens
        String newAccessToken = jwtTokenProvider.createAccessToken(authentication);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(authentication);

        return new TokenResponseDto(newAccessToken, newRefreshToken);
    }
}
