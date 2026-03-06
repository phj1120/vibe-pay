package com.api.app.common.security

import com.api.app.common.exception.ApiError
import com.api.app.common.jwt.JwtTokenProvider
import com.api.app.common.response.ApiResponse
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val customUserDetailsService: CustomUserDetailsService,
    private val objectMapper: ObjectMapper
) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val token = extractTokenFromRequest(request)
            if (token != null) {
                if (jwtTokenProvider.isTokenExpired(token)) {
                    log.warn("토큰이 만료되었습니다")
                    sendUnauthorizedResponse(response, ApiError.EXPIRED_TOKEN)
                    return
                }
                val email = jwtTokenProvider.extractEmail(token)
                val userDetails = customUserDetailsService.loadUserByUsername(email)
                if (jwtTokenProvider.validateToken(token, userDetails)) {
                    val authentication = UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.authorities
                    )
                    authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                    SecurityContextHolder.getContext().authentication = authentication
                } else {
                    log.warn("유효하지 않은 토큰입니다")
                    sendUnauthorizedResponse(response, ApiError.INVALID_TOKEN)
                    return
                }
            }
        } catch (ex: Exception) {
            log.error("토큰 검증 중 오류 발생", ex)
            sendUnauthorizedResponse(response, ApiError.INVALID_TOKEN)
            return
        }
        filterChain.doFilter(request, response)
    }

    private fun sendUnauthorizedResponse(response: HttpServletResponse, apiError: ApiError) {
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = "application/json;charset=UTF-8"
        response.writer.write(objectMapper.writeValueAsString(ApiResponse.error<Void>(apiError)))
    }

    private fun extractTokenFromRequest(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        return if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else null
    }
}
