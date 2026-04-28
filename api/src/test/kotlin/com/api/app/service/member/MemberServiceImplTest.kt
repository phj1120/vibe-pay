package com.api.app.service.member

import com.api.app.common.exception.ApiError
import com.api.app.common.exception.ApiException
import com.api.app.common.jwt.JwtTokenProvider
import com.api.app.dto.request.member.MemberLoginRequest
import com.api.app.dto.request.member.MemberRegisterRequest
import com.api.app.entity.MemberBase
import com.api.app.repository.rodb.member.MemberBaseRepository
import com.api.app.repository.rwdb.member.MemberBaseTrxRepository
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
    private lateinit var memberBaseRepository: MemberBaseRepository

    @Mock
    private lateinit var memberBaseTrxRepository: MemberBaseTrxRepository

    @Mock
    private lateinit var passwordEncoder: PasswordEncoder

    @Mock
    private lateinit var jwtTokenProvider: JwtTokenProvider

    @Test
    @DisplayName("회원 가입 성공")
    fun registerMember_Success() {
        val request = MemberRegisterRequest(
            memberName = "홍길동",
            phone = "010-1234-5678",
            email = "hong@example.com",
            password = "password123"
        )

        given(memberBaseRepository.findByEmail(anyString())).willReturn(null)
        given(passwordEncoder.encode(anyString())).willReturn("encodedPassword")
        given(memberBaseTrxRepository.save(any(MemberBase::class.java))).willAnswer { it.arguments[0] as MemberBase }

        memberService.registerMember(request)

        verify(memberBaseRepository, times(1)).findByEmail(request.email)
        verify(passwordEncoder, times(1)).encode(request.password)
        verify(memberBaseTrxRepository, times(1)).save(any(MemberBase::class.java))
    }

    @Test
    @DisplayName("회원 가입 실패 - 이메일 중복")
    fun registerMember_Fail_DuplicateEmail() {
        val request = MemberRegisterRequest(
            memberName = "홍길동",
            phone = "010-1234-5678",
            email = "hong@example.com",
            password = "password123"
        )

        given(memberBaseRepository.findByEmail(anyString())).willReturn(MemberBase().apply { email = "hong@example.com" })

        assertThatThrownBy { memberService.registerMember(request) }
            .isInstanceOf(ApiException::class.java)
            .hasFieldOrPropertyWithValue("apiError", ApiError.DUPLICATE_EMAIL)

        verify(memberBaseRepository, times(1)).findByEmail(request.email)
        verify(memberBaseTrxRepository, never()).save(any(MemberBase::class.java))
    }

    @Test
    @DisplayName("로그인 성공")
    fun login_Success() {
        val request = MemberLoginRequest(email = "hong@example.com", password = "password123")

        val member = MemberBase().apply {
            email = "hong@example.com"
            password = "encodedPassword"
        }

        given(memberBaseRepository.findByEmail(anyString())).willReturn(member)
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true)
        given(jwtTokenProvider.generateAccessToken(anyString())).willReturn("accessToken")
        given(jwtTokenProvider.generateRefreshToken(anyString())).willReturn("refreshToken")

        val response = memberService.login(request)

        assertThat(response.accessToken).isEqualTo("accessToken")
        assertThat(response.refreshToken).isEqualTo("refreshToken")

        verify(memberBaseRepository, times(1)).findByEmail(request.email)
        verify(passwordEncoder, times(1)).matches(request.password, member.password)
        verify(jwtTokenProvider, times(1)).generateAccessToken(member.email)
        verify(jwtTokenProvider, times(1)).generateRefreshToken(member.email)
    }

    @Test
    @DisplayName("로그인 실패 - 회원 정보 없음")
    fun login_Fail_MemberNotFound() {
        val request = MemberLoginRequest(email = "hong@example.com", password = "password123")

        given(memberBaseRepository.findByEmail(anyString())).willReturn(null)

        assertThatThrownBy { memberService.login(request) }
            .isInstanceOf(ApiException::class.java)
            .hasFieldOrPropertyWithValue("apiError", ApiError.INVALID_CREDENTIALS)

        verify(memberBaseRepository, times(1)).findByEmail(request.email)
        verify(passwordEncoder, never()).matches(anyString(), anyString())
        verify(jwtTokenProvider, never()).generateAccessToken(anyString())
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    fun login_Fail_InvalidPassword() {
        val request = MemberLoginRequest(email = "hong@example.com", password = "wrongPassword")

        val member = MemberBase().apply {
            email = "hong@example.com"
            password = "encodedPassword"
        }

        given(memberBaseRepository.findByEmail(anyString())).willReturn(member)
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false)

        assertThatThrownBy { memberService.login(request) }
            .isInstanceOf(ApiException::class.java)
            .hasFieldOrPropertyWithValue("apiError", ApiError.INVALID_CREDENTIALS)

        verify(memberBaseRepository, times(1)).findByEmail(request.email)
        verify(passwordEncoder, times(1)).matches(request.password, member.password)
        verify(jwtTokenProvider, never()).generateAccessToken(anyString())
    }

    @Test
    @DisplayName("회원 정보 조회 성공")
    fun getMemberInfo_Success() {
        val email = "hong@example.com"

        val member = MemberBase().apply {
            memberName = "홍길동"
            phone = "010-1234-5678"
            this.email = email
            memberStatusCode = "001"
        }

        given(memberBaseRepository.findByEmail(anyString())).willReturn(member)

        val response = memberService.getMemberInfo(email)

        assertThat(response.memberName).isEqualTo("홍길동")
        assertThat(response.phone).isEqualTo("010-1234-5678")
        assertThat(response.email).isEqualTo(email)
        assertThat(response.memberStatusCode).isEqualTo("001")

        verify(memberBaseRepository, times(1)).findByEmail(email)
    }

    @Test
    @DisplayName("회원 정보 조회 실패 - 회원 정보 없음")
    fun getMemberInfo_Fail_MemberNotFound() {
        val email = "hong@example.com"

        given(memberBaseRepository.findByEmail(anyString())).willReturn(null)

        assertThatThrownBy { memberService.getMemberInfo(email) }
            .isInstanceOf(ApiException::class.java)
            .hasFieldOrPropertyWithValue("apiError", ApiError.DATA_NOT_FOUND)

        verify(memberBaseRepository, times(1)).findByEmail(email)
    }
}
