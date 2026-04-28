package com.api.app.service.member

import com.api.app.common.exception.ApiError
import com.api.app.common.exception.ApiException
import com.api.app.common.jwt.JwtTokenProvider
import com.api.app.dto.request.member.MemberLoginRequest
import com.api.app.dto.request.member.MemberRegisterRequest
import com.api.app.dto.request.member.TokenRefreshRequest
import com.api.app.entity.MemberBase
import com.api.app.repository.rodb.member.MemberBaseRepository
import com.api.app.repository.rwdb.member.MemberBaseTrxRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.crypto.password.PasswordEncoder

@ExtendWith(MockitoExtension::class)
class MemberServiceImplTest {

    @InjectMocks
    private lateinit var memberService: MemberServiceImpl

    @Mock
    private lateinit var memberBaseRepository: MemberBaseRepository

    @Mock
    private lateinit var memberBaseTrxRepository: MemberBaseTrxRepository

    @Mock
    private lateinit var passwordEncoder: PasswordEncoder

    @Mock
    private lateinit var jwtTokenProvider: JwtTokenProvider

    @Test
    @DisplayName("회원 가입 성공")
    fun registerMemberSuccess() {
        val request = MemberRegisterRequest(
            memberName = "홍길동",
            phone = "010-1234-5678",
            email = "hong@example.com",
            password = "password123"
        )
        given(memberBaseRepository.findByEmail(request.email)).willReturn(null)
        given(passwordEncoder.encode(request.password)).willReturn("encoded-password")

        memberService.registerMember(request)

        val captor = ArgumentCaptor.forClass(MemberBase::class.java)
        verify(memberBaseTrxRepository).save(captor.capture())
        assertThat(captor.value.memberName).isEqualTo(request.memberName)
        assertThat(captor.value.email).isEqualTo(request.email)
        assertThat(captor.value.password).isEqualTo("encoded-password")
        assertThat(captor.value.memberStatusCode).isEqualTo("001")
    }

    @Test
    @DisplayName("회원 가입 실패 - 이메일 중복")
    fun registerMemberFailWhenDuplicateEmail() {
        val request = MemberRegisterRequest(
            memberName = "홍길동",
            phone = "010-1234-5678",
            email = "hong@example.com",
            password = "password123"
        )
        given(memberBaseRepository.findByEmail(request.email)).willReturn(createMember(email = request.email))

        assertThatThrownBy { memberService.registerMember(request) }
            .isInstanceOf(ApiException::class.java)
            .hasFieldOrPropertyWithValue("apiError", ApiError.DUPLICATE_EMAIL)

        verify(memberBaseTrxRepository, never()).save(any(MemberBase::class.java))
    }

    @Test
    @DisplayName("로그인 성공")
    fun loginSuccess() {
        val member = createMember(email = "hong@example.com").apply { password = "encoded-password" }
        val request = MemberLoginRequest(email = member.email, password = "password123")
        given(memberBaseRepository.findByEmail(request.email)).willReturn(member)
        given(passwordEncoder.matches(request.password, member.password)).willReturn(true)
        given(jwtTokenProvider.generateAccessToken(member.email)).willReturn("access-token")
        given(jwtTokenProvider.generateRefreshToken(member.email)).willReturn("refresh-token")

        val response = memberService.login(request)

        assertThat(response.accessToken).isEqualTo("access-token")
        assertThat(response.refreshToken).isEqualTo("refresh-token")
    }

    @Test
    @DisplayName("로그인 실패 - 회원 없음")
    fun loginFailWhenMemberMissing() {
        val request = MemberLoginRequest(email = "hong@example.com", password = "password123")
        given(memberBaseRepository.findByEmail(request.email)).willReturn(null)

        assertThatThrownBy { memberService.login(request) }
            .isInstanceOf(ApiException::class.java)
            .hasFieldOrPropertyWithValue("apiError", ApiError.INVALID_CREDENTIALS)
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    fun loginFailWhenPasswordMismatch() {
        val member = createMember(email = "hong@example.com").apply { password = "encoded-password" }
        val request = MemberLoginRequest(email = member.email, password = "wrong-password")
        given(memberBaseRepository.findByEmail(request.email)).willReturn(member)
        given(passwordEncoder.matches(request.password, member.password)).willReturn(false)

        assertThatThrownBy { memberService.login(request) }
            .isInstanceOf(ApiException::class.java)
            .hasFieldOrPropertyWithValue("apiError", ApiError.INVALID_CREDENTIALS)

        verify(jwtTokenProvider, never()).generateAccessToken(member.email)
    }

    @Test
    @DisplayName("회원 정보 조회 성공")
    fun getMemberInfoSuccess() {
        val member = createMember(email = "hong@example.com")
        given(memberBaseRepository.findByEmail(member.email)).willReturn(member)

        val response = memberService.getMemberInfo(member.email)

        assertThat(response.memberName).isEqualTo(member.memberName)
        assertThat(response.phone).isEqualTo(member.phone)
        assertThat(response.memberStatusCode).isEqualTo(member.memberStatusCode)
    }

    @Test
    @DisplayName("회원 정보 조회 실패 - 회원 없음")
    fun getMemberInfoFailWhenMissing() {
        given(memberBaseRepository.findByEmail("missing@example.com")).willReturn(null)

        assertThatThrownBy { memberService.getMemberInfo("missing@example.com") }
            .isInstanceOf(ApiException::class.java)
            .hasFieldOrPropertyWithValue("apiError", ApiError.DATA_NOT_FOUND)
    }

    @Test
    @DisplayName("토큰 갱신 성공")
    fun refreshTokenSuccess() {
        val request = TokenRefreshRequest(refreshToken = "refresh-token")
        val member = createMember(email = "hong@example.com")
        given(jwtTokenProvider.isTokenExpired(request.refreshToken)).willReturn(false)
        given(jwtTokenProvider.extractEmail(request.refreshToken)).willReturn(member.email)
        given(memberBaseRepository.findByEmail(member.email)).willReturn(member)
        given(jwtTokenProvider.generateAccessToken(member.email)).willReturn("new-access-token")
        given(jwtTokenProvider.generateRefreshToken(member.email)).willReturn("new-refresh-token")

        val response = memberService.refreshToken(request)

        assertThat(response.accessToken).isEqualTo("new-access-token")
        assertThat(response.refreshToken).isEqualTo("new-refresh-token")
        verify(jwtTokenProvider, times(1)).extractEmail(request.refreshToken)
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 만료된 토큰")
    fun refreshTokenFailWhenExpired() {
        val request = TokenRefreshRequest(refreshToken = "expired-token")
        given(jwtTokenProvider.isTokenExpired(request.refreshToken)).willReturn(true)

        assertThatThrownBy { memberService.refreshToken(request) }
            .isInstanceOf(ApiException::class.java)
            .hasFieldOrPropertyWithValue("apiError", ApiError.EXPIRED_TOKEN)

        verify(jwtTokenProvider, never()).extractEmail(request.refreshToken)
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 잘못된 토큰")
    fun refreshTokenFailWhenInvalid() {
        val request = TokenRefreshRequest(refreshToken = "invalid-token")
        given(jwtTokenProvider.isTokenExpired(request.refreshToken)).willReturn(false)
        given(jwtTokenProvider.extractEmail(request.refreshToken)).willThrow(IllegalArgumentException("bad token"))

        assertThatThrownBy { memberService.refreshToken(request) }
            .isInstanceOf(ApiException::class.java)
            .hasFieldOrPropertyWithValue("apiError", ApiError.INVALID_TOKEN)
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 회원 없음")
    fun refreshTokenFailWhenMemberMissing() {
        val request = TokenRefreshRequest(refreshToken = "refresh-token")
        given(jwtTokenProvider.isTokenExpired(request.refreshToken)).willReturn(false)
        given(jwtTokenProvider.extractEmail(request.refreshToken)).willReturn("missing@example.com")
        given(memberBaseRepository.findByEmail("missing@example.com")).willReturn(null)

        assertThatThrownBy { memberService.refreshToken(request) }
            .isInstanceOf(ApiException::class.java)
            .hasFieldOrPropertyWithValue("apiError", ApiError.DATA_NOT_FOUND)
    }

    private fun createMember(
        memberNo: String = "M001",
        email: String = "hong@example.com"
    ) = MemberBase().apply {
        this.memberNo = memberNo
        this.memberName = "홍길동"
        this.phone = "010-1234-5678"
        this.email = email
        this.password = "encoded-password"
        this.memberStatusCode = "001"
    }
}
