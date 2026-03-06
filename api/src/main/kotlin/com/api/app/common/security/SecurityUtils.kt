package com.api.app.common.security

import com.api.app.common.exception.ApiError
import com.api.app.common.exception.ApiException
import com.api.app.entity.MemberBase
import com.api.app.repository.rodb.member.MemberBaseRepository
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component

@Component
class SecurityUtils(private val memberBaseRepository: MemberBaseRepository) {

    private val log = LoggerFactory.getLogger(this::class.java)

    fun getCurrentUserEmail(): String {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated) {
            throw ApiException(ApiError.UNAUTHORIZED, "인증되지 않은 사용자입니다")
        }
        val principal = authentication.principal
        if (principal is UserDetails) {
            return principal.username
        }
        throw ApiException(ApiError.UNAUTHORIZED, "인증 정보가 올바르지 않습니다")
    }

    fun getCurrentUserMemberNo(): String {
        val email = getCurrentUserEmail()
        val member = memberBaseRepository.findByEmail(email)
            ?: throw ApiException(ApiError.DATA_NOT_FOUND, "회원 정보를 찾을 수 없습니다")
        return member.memberNo
    }

    fun getCurrentUser(): MemberBase {
        val email = getCurrentUserEmail()
        return memberBaseRepository.findByEmail(email)
            ?: throw ApiException(ApiError.DATA_NOT_FOUND, "회원 정보를 찾을 수 없습니다")
    }
}
