package com.api.app.common.security

import com.api.app.client.CoreFeignClient
import com.api.app.common.exception.ApiError
import com.api.app.common.exception.ApiException
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class SecurityUtils(private val coreFeignClient: CoreFeignClient) {

    private val log = LoggerFactory.getLogger(this::class.java)

    fun getCurrentUserEmail(): String {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated) {
            throw ApiException(ApiError.UNAUTHORIZED, "인증되지 않은 사용자입니다")
        }
        return authentication.principal as? String
            ?: throw ApiException(ApiError.UNAUTHORIZED, "인증 정보가 올바르지 않습니다")
    }

    fun getCurrentUserMemberNo(): String {
        val email = getCurrentUserEmail()
        return coreFeignClient.getMemberByEmail(email).data?.memberNo
            ?: throw ApiException(ApiError.DATA_NOT_FOUND, "회원 정보를 찾을 수 없습니다")
    }
}
