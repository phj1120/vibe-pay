package com.api.app.service.member

import com.api.app.common.exception.ApiError
import com.api.app.common.exception.ApiException
import com.api.app.common.jwt.JwtTokenProvider
import com.api.app.dto.request.member.MemberLoginRequest
import com.api.app.dto.request.member.MemberRegisterRequest
import com.api.app.dto.request.member.TokenRefreshRequest
import com.api.app.dto.response.member.MemberInfoResponse
import com.api.app.dto.response.member.MemberLoginResponse
import com.api.app.dto.response.member.TokenRefreshResponse
import com.api.app.entity.MemberBase
import com.api.app.repository.rodb.member.MemberBaseRepository
import com.api.app.repository.rwdb.member.MemberBaseTrxRepository
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class MemberServiceImpl(
    private val memberBaseRepository: MemberBaseRepository,
    private val memberBaseTrxRepository: MemberBaseTrxRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider
) : MemberService {

    private val log = LoggerFactory.getLogger(this::class.java)

    @Transactional
    override fun registerMember(request: MemberRegisterRequest) {
        log.debug("회원 가입 시작: {}", request.email)

        if (memberBaseRepository.findByEmail(request.email) != null) {
            throw ApiException(ApiError.DUPLICATE_EMAIL)
        }

        val memberNo = memberBaseTrxRepository.generateMemberNo()

        val memberBase = MemberBase().apply {
            this.memberNo = memberNo
            this.memberName = request.memberName
            this.phone = request.phone
            this.email = request.email
            this.password = passwordEncoder.encode(request.password)
            this.memberStatusCode = "001"
        }

        memberBaseTrxRepository.save(memberBase)
        log.info("회원 가입 완료: memberNo={}, email={}", memberNo, request.email)
    }

    override fun login(request: MemberLoginRequest): MemberLoginResponse {
        log.debug("로그인 시도: {}", request.email)

        val member = memberBaseRepository.findByEmail(request.email)
            ?: throw ApiException(ApiError.INVALID_CREDENTIALS)

        if (!passwordEncoder.matches(request.password, member.password)) {
            throw ApiException(ApiError.INVALID_CREDENTIALS)
        }

        log.info("로그인 성공: email={}", request.email)
        return MemberLoginResponse(
            accessToken = jwtTokenProvider.generateAccessToken(member.email),
            refreshToken = jwtTokenProvider.generateRefreshToken(member.email)
        )
    }

    override fun getMemberInfo(email: String): MemberInfoResponse {
        val member = memberBaseRepository.findByEmail(email)
            ?: throw ApiException(ApiError.DATA_NOT_FOUND, "회원 정보를 찾을 수 없습니다")

        return MemberInfoResponse(
            memberName = member.memberName,
            phone = member.phone,
            email = member.email,
            memberStatusCode = member.memberStatusCode
        )
    }

    override fun refreshToken(request: TokenRefreshRequest): TokenRefreshResponse {
        val refreshToken = request.refreshToken

        if (jwtTokenProvider.isTokenExpired(refreshToken)) {
            throw ApiException(ApiError.EXPIRED_TOKEN, "리프레시 토큰이 만료되었습니다")
        }

        val email = try {
            jwtTokenProvider.extractEmail(refreshToken)
        } catch (e: Exception) {
            throw ApiException(ApiError.INVALID_TOKEN, "유효하지 않은 토큰입니다")
        }

        memberBaseRepository.findByEmail(email)
            ?: throw ApiException(ApiError.DATA_NOT_FOUND, "회원 정보를 찾을 수 없습니다")

        log.info("토큰 갱신 성공: email={}", email)
        return TokenRefreshResponse(
            accessToken = jwtTokenProvider.generateAccessToken(email),
            refreshToken = jwtTokenProvider.generateRefreshToken(email)
        )
    }
}
