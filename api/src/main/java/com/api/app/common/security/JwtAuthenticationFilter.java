package com.api.app.common.security;

import com.api.app.common.exception.ApiError;
import com.api.app.common.jwt.JwtTokenProvider;
import com.api.app.common.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * @author system
 * @version 1.0
 * @since 2025-10-28
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String token = extractTokenFromRequest(request);

            if (token != null) {
                // 토큰 만료 체크
                if (jwtTokenProvider.isTokenExpired(token)) {
                    log.warn("토큰이 만료되었습니다");
                    sendUnauthorizedResponse(response, ApiError.EXPIRED_TOKEN);
                    return;
                }

                // 토큰 검증 및 인증 설정
                String email = jwtTokenProvider.extractEmail(token);
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

                if (jwtTokenProvider.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    log.warn("유효하지 않은 토큰입니다");
                    sendUnauthorizedResponse(response, ApiError.INVALID_TOKEN);
                    return;
                }
            }
        } catch (Exception ex) {
            log.error("토큰 검증 중 오류 발생", ex);
            sendUnauthorizedResponse(response, ApiError.INVALID_TOKEN);
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 401 Unauthorized 응답 전송
     *
     * @param response HttpServletResponse
     * @param apiError ApiError
     * @throws IOException
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, ApiError apiError) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        ApiResponse<Void> apiResponse = ApiResponse.error(apiError);
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }

    /**
     * Request에서 JWT 토큰 추출
     *
     * @param request HttpServletRequest
     * @return JWT 토큰
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}
