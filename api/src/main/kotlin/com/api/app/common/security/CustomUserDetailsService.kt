package com.api.app.common.security

import com.api.app.common.exception.ApiError
import com.api.app.common.exception.ApiException
import com.api.app.repository.rodb.member.MemberBaseRepository
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val memberBaseRepository: MemberBaseRepository
) : UserDetailsService {

    override fun loadUserByUsername(email: String): UserDetails {
        val member = memberBaseRepository.findByEmail(email)
            ?: throw ApiException(ApiError.DATA_NOT_FOUND, "회원 정보를 찾을 수 없습니다")
        return User(member.email, member.password, emptyList())
    }
}
