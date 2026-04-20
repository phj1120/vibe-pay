package com.vibepay.core.service

import com.api.app.common.exception.ApiError
import com.api.app.common.exception.ApiException
import com.api.app.common.jwt.JwtTokenProvider
import com.vibepay.core.dto.request.member.MemberLoginRequest
import com.vibepay.core.dto.request.member.MemberRegisterRequest
import com.vibepay.core.dto.request.member.TokenRefreshRequest
import com.vibepay.core.entity.MemberBase
import com.vibepay.core.repository.MemberRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.crypto.password.PasswordEncoder

@ExtendWith(MockitoExtension::class)
class MemberServiceImplTest {

    @InjectMocks
    private lateinit var memberService: MemberServiceImpl

    @Mock
    private lateinit var memberRepository: MemberRepository

    @Mock
    private lateinit var passwordEncoder: PasswordEncoder

    @Mock
    private lateinit var jwtTokenProvider: JwtTokenProvider

    private fun member(email: String = "hong@example.com") = MemberBase().apply {
        this.memberNo = "000000000000001"
        this.email = email
        this.memberName = "홍길동"
        this.phone = "010-1234-5678"
        this.password = "encodedPassword"
        this.memberStatusCode = "001"
    }

    @Test
    @DisplayName("회원 가입 성공")
    fun registerMember_Success() {
        val request = MemberRegisterRequest(memberName = "홍길동", phone = "010-1234-5678", email = "hong@example.com", password = "password123")

        given(memberRepository.findByEmail(anyString())).willReturn(null)
        given(passwordEncoder.encode(anyString())).willReturn("encodedPassword")
        given(memberRepository.save(any(MemberBase::class.java))).willAnswer { it.arguments[0] as MemberBase }

        memberService.registerMember(request)

        verify(memberRepository, times(1)).findByEmail(request.email)
        verify(passwordEncoder, times(1)).encode(request.password)
        verify(memberRepository, times(1)).save(any(MemberBase::class.java))
    }

    @Test
    @DisplayName("회원 가입 실패 - 이메일 중복")
    fun registerMember_Fail_DuplicateEmail() {
        val request = MemberRegisterRequest(memberName = "홍길동", phone = "010-1234-5678", email = "hong@example.com", password = "password123")

        given(memberRepository.findByEmail(anyString())).willReturn(member())

        assertThatThrownBy { memberService.registerMember(request) }
            .isInstanceOf(ApiException::class.java)
            .hasFieldOrPropertyWithValue("apiError", ApiError.DUPLICATE_EMAIL)

        verify(memberRepository, never()).save(any(MemberBase::class.java))
    }

    @Test
    @DisplayName("로그인 성공")
    fun login_Success() {
        val request = MemberLoginRequest(email = "hong@example.com", password = "password123")

        given(memberRepository.findByEmail(anyString())).willReturn(member())
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true)
        given(jwtTokenProvider.generateAccessToken(anyString())).willReturn("accessToken")
        given(jwtTokenProvider.generateRefreshToken(anyString())).willReturn("refreshToken")

        val response = memberService.login(request)

        assertThat(response.accessToken).isEqualTo("accessToken")
        assertThat(response.refreshToken).isEqualTo("refreshToken")
    }

    @Test
    @DisplayName("로그인 실패 - 회원 없음")
    fun login_Fail_MemberNotFound() {
        given(memberRepository.findByEmail(anyString())).willReturn(null)

        assertThatThrownBy { memberService.login(MemberLoginRequest("hong@example.com", "pass")) }
            .isInstanceOf(ApiException::class.java)
            .hasFieldOrPropertyWithValue("apiError", ApiError.INVALID_CREDENTIALS)

        verify(jwtTokenProvider, never()).generateAccessToken(anyString())
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    fun login_Fail_WrongPassword() {
        given(memberRepository.findByEmail(anyString())).willReturn(member())
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false)

        assertThatThrownBy { memberService.login(MemberLoginRequest("hong@example.com", "wrong")) }
            .isInstanceOf(ApiException::class.java)
            .hasFieldOrPropertyWithValue("apiError", ApiError.INVALID_CREDENTIALS)
    }

    @Test
    @DisplayName("회원 정보 조회 성공")
    fun getMemberInfo_Success() {
        given(memberRepository.findByEmail(anyString())).willReturn(member())

        val response = memberService.getMemberInfo("hong@example.com")

        assertThat(response.memberName).isEqualTo("홍길동")
        assertThat(response.email).isEqualTo("hong@example.com")
    }

    @Test
    @DisplayName("회원 정보 조회 실패 - 회원 없음")
    fun getMemberInfo_Fail_NotFound() {
        given(memberRepository.findByEmail(anyString())).willReturn(null)

        assertThatThrownBy { memberService.getMemberInfo("hong@example.com") }
            .isInstanceOf(ApiException::class.java)
            .hasFieldOrPropertyWithValue("apiError", ApiError.DATA_NOT_FOUND)
    }

    @Test
    @DisplayName("토큰 갱신 성공")
    fun refreshToken_Success() {
        val refreshToken = "validRefreshToken"
        given(jwtTokenProvider.isTokenExpired(refreshToken)).willReturn(false)
        given(jwtTokenProvider.extractEmail(refreshToken)).willReturn("hong@example.com")
        given(memberRepository.findByEmail(anyString())).willReturn(member())
        given(jwtTokenProvider.generateAccessToken(anyString())).willReturn("newAccessToken")
        given(jwtTokenProvider.generateRefreshToken(anyString())).willReturn("newRefreshToken")

        val response = memberService.refreshToken(TokenRefreshRequest(refreshToken))

        assertThat(response.accessToken).isEqualTo("newAccessToken")
    }
}
