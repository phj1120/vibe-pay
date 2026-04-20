package com.vibepay.core.service

import com.api.app.common.exception.ApiError
import com.api.app.common.exception.ApiException
import com.api.app.common.jwt.JwtTokenProvider
import com.vibepay.core.dto.request.member.MemberLoginRequest
import com.vibepay.core.dto.request.member.MemberRegisterRequest
import com.vibepay.core.dto.request.member.TokenRefreshRequest
import com.vibepay.core.dto.response.member.MemberInfoResponse
import com.vibepay.core.dto.response.member.MemberLoginResponse
import com.vibepay.core.dto.response.member.TokenRefreshResponse
import com.vibepay.core.entity.MemberBase
import com.vibepay.core.repository.MemberRepository
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class MemberServiceImpl(
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider
) : MemberService {

    private val log = LoggerFactory.getLogger(this::class.java)

    @Transactional
    override fun registerMember(request: MemberRegisterRequest) {
        if (memberRepository.findByEmail(request.email) != null) throw ApiException(ApiError.DUPLICATE_EMAIL)
        val member = MemberBase().apply {
            memberName = request.memberName
            phone = request.phone
            email = request.email
            password = passwordEncoder.encode(request.password)
            memberStatusCode = "001"
        }
        memberRepository.save(member)
        log.info("회원 가입 완료: memberNo={}, email={}", member.memberNo, request.email)
    }

    override fun login(request: MemberLoginRequest): MemberLoginResponse {
        val member = memberRepository.findByEmail(request.email) ?: throw ApiException(ApiError.INVALID_CREDENTIALS)
        if (!passwordEncoder.matches(request.password, member.password)) throw ApiException(ApiError.INVALID_CREDENTIALS)
        return MemberLoginResponse(
            accessToken = jwtTokenProvider.generateAccessToken(member.email),
            refreshToken = jwtTokenProvider.generateRefreshToken(member.email)
        )
    }

    override fun getMemberInfo(email: String): MemberInfoResponse {
        val member = memberRepository.findByEmail(email) ?: throw ApiException(ApiError.DATA_NOT_FOUND, "회원 정보를 찾을 수 없습니다")
        return MemberInfoResponse(
            memberNo = member.memberNo, memberName = member.memberName,
            phone = member.phone, email = member.email, memberStatusCode = member.memberStatusCode
        )
    }

    override fun refreshToken(request: TokenRefreshRequest): TokenRefreshResponse {
        if (jwtTokenProvider.isTokenExpired(request.refreshToken)) throw ApiException(ApiError.EXPIRED_TOKEN, "리프레시 토큰이 만료되었습니다")
        val email = try {
            jwtTokenProvider.extractEmail(request.refreshToken)
        } catch (e: Exception) {
            throw ApiException(ApiError.INVALID_TOKEN, "유효하지 않은 토큰입니다")
        }
        memberRepository.findByEmail(email) ?: throw ApiException(ApiError.DATA_NOT_FOUND, "회원 정보를 찾을 수 없습니다")
        return TokenRefreshResponse(
            accessToken = jwtTokenProvider.generateAccessToken(email),
            refreshToken = jwtTokenProvider.generateRefreshToken(email)
        )
    }

    override fun findByEmail(email: String): MemberBase? = memberRepository.findByEmail(email)
    override fun findByMemberNo(memberNo: String): MemberBase? = memberRepository.findByMemberNo(memberNo)
}
