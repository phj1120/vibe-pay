package com.api.app.service.member;

import com.api.app.common.exception.ApiError;
import com.api.app.common.exception.ApiException;
import com.api.app.common.jwt.JwtTokenProvider;
import com.api.app.dto.request.member.MemberLoginRequest;
import com.api.app.dto.request.member.MemberRegisterRequest;
import com.api.app.dto.response.member.MemberInfoResponse;
import com.api.app.dto.response.member.MemberLoginResponse;
import com.api.app.entity.MemberBase;
import com.api.app.repository.member.MemberBaseMapper;
import com.api.app.repository.member.MemberBaseTrxMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * @author system
 * @version 1.0
 * @since 2025-10-28
 */
@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

    @InjectMocks
    private MemberServiceImpl memberService;

    @Mock
    private MemberBaseMapper memberBaseMapper;

    @Mock
    private MemberBaseTrxMapper memberBaseTrxMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("회원 가입 성공")
    void registerMember_Success() {
        // given
        MemberRegisterRequest request = new MemberRegisterRequest();
        request.setMemberName("홍길동");
        request.setPhone("010-1234-5678");
        request.setEmail("hong@example.com");
        request.setPassword("password123");

        given(memberBaseMapper.selectMemberBaseByEmail(anyString())).willReturn(null);
        given(memberBaseTrxMapper.generateMemberNo()).willReturn("000000000000001");
        given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
        given(memberBaseTrxMapper.insertMemberBase(any(MemberBase.class))).willReturn(1);

        // when
        memberService.registerMember(request);

        // then
        verify(memberBaseMapper, times(1)).selectMemberBaseByEmail(request.getEmail());
        verify(memberBaseTrxMapper, times(1)).generateMemberNo();
        verify(passwordEncoder, times(1)).encode(request.getPassword());
        verify(memberBaseTrxMapper, times(1)).insertMemberBase(any(MemberBase.class));
    }

    @Test
    @DisplayName("회원 가입 실패 - 이메일 중복")
    void registerMember_Fail_DuplicateEmail() {
        // given
        MemberRegisterRequest request = new MemberRegisterRequest();
        request.setEmail("hong@example.com");

        MemberBase existingMember = new MemberBase();
        existingMember.setEmail("hong@example.com");

        given(memberBaseMapper.selectMemberBaseByEmail(anyString())).willReturn(existingMember);

        // when & then
        assertThatThrownBy(() -> memberService.registerMember(request))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("apiError", ApiError.DUPLICATE_EMAIL);

        verify(memberBaseMapper, times(1)).selectMemberBaseByEmail(request.getEmail());
        verify(memberBaseTrxMapper, never()).generateMemberNo();
        verify(memberBaseTrxMapper, never()).insertMemberBase(any(MemberBase.class));
    }

    @Test
    @DisplayName("로그인 성공")
    void login_Success() {
        // given
        MemberLoginRequest request = new MemberLoginRequest();
        request.setEmail("hong@example.com");
        request.setPassword("password123");

        MemberBase member = new MemberBase();
        member.setEmail("hong@example.com");
        member.setPassword("encodedPassword");

        given(memberBaseMapper.selectMemberBaseByEmail(anyString())).willReturn(member);
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);
        given(jwtTokenProvider.generateAccessToken(anyString())).willReturn("accessToken");
        given(jwtTokenProvider.generateRefreshToken(anyString())).willReturn("refreshToken");

        // when
        MemberLoginResponse response = memberService.login(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("accessToken");
        assertThat(response.getRefreshToken()).isEqualTo("refreshToken");

        verify(memberBaseMapper, times(1)).selectMemberBaseByEmail(request.getEmail());
        verify(passwordEncoder, times(1)).matches(request.getPassword(), member.getPassword());
        verify(jwtTokenProvider, times(1)).generateAccessToken(member.getEmail());
        verify(jwtTokenProvider, times(1)).generateRefreshToken(member.getEmail());
    }

    @Test
    @DisplayName("로그인 실패 - 회원 정보 없음")
    void login_Fail_MemberNotFound() {
        // given
        MemberLoginRequest request = new MemberLoginRequest();
        request.setEmail("hong@example.com");
        request.setPassword("password123");

        given(memberBaseMapper.selectMemberBaseByEmail(anyString())).willReturn(null);

        // when & then
        assertThatThrownBy(() -> memberService.login(request))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("apiError", ApiError.INVALID_CREDENTIALS);

        verify(memberBaseMapper, times(1)).selectMemberBaseByEmail(request.getEmail());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtTokenProvider, never()).generateAccessToken(anyString());
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_Fail_InvalidPassword() {
        // given
        MemberLoginRequest request = new MemberLoginRequest();
        request.setEmail("hong@example.com");
        request.setPassword("wrongPassword");

        MemberBase member = new MemberBase();
        member.setEmail("hong@example.com");
        member.setPassword("encodedPassword");

        given(memberBaseMapper.selectMemberBaseByEmail(anyString())).willReturn(member);
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

        // when & then
        assertThatThrownBy(() -> memberService.login(request))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("apiError", ApiError.INVALID_CREDENTIALS);

        verify(memberBaseMapper, times(1)).selectMemberBaseByEmail(request.getEmail());
        verify(passwordEncoder, times(1)).matches(request.getPassword(), member.getPassword());
        verify(jwtTokenProvider, never()).generateAccessToken(anyString());
    }

    @Test
    @DisplayName("회원 정보 조회 성공")
    void getMemberInfo_Success() {
        // given
        String email = "hong@example.com";

        MemberBase member = new MemberBase();
        member.setMemberName("홍길동");
        member.setPhone("010-1234-5678");
        member.setEmail(email);
        member.setMemberStatusCode("001");

        given(memberBaseMapper.selectMemberBaseByEmail(anyString())).willReturn(member);

        // when
        MemberInfoResponse response = memberService.getMemberInfo(email);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getMemberName()).isEqualTo("홍길동");
        assertThat(response.getPhone()).isEqualTo("010-1234-5678");
        assertThat(response.getEmail()).isEqualTo(email);
        assertThat(response.getMemberStatusCode()).isEqualTo("001");

        verify(memberBaseMapper, times(1)).selectMemberBaseByEmail(email);
    }

    @Test
    @DisplayName("회원 정보 조회 실패 - 회원 정보 없음")
    void getMemberInfo_Fail_MemberNotFound() {
        // given
        String email = "hong@example.com";

        given(memberBaseMapper.selectMemberBaseByEmail(anyString())).willReturn(null);

        // when & then
        assertThatThrownBy(() -> memberService.getMemberInfo(email))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("apiError", ApiError.DATA_NOT_FOUND);

        verify(memberBaseMapper, times(1)).selectMemberBaseByEmail(email);
    }
}
