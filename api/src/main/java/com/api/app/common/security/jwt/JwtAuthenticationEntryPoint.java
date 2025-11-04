package com.api.app.common.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        // 유효한 자격증명을 제공하지 않고 접근하려 할 때 401
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, String> errorResponse = new HashMap<>();
        // TODO: More specific error codes based on JwtTokenProvider validation result
        errorResponse.put("code", "2002"); // Default to invalid token
        errorResponse.put("message", "유효하지 않은 토큰입니다");

        // Check if it's an expired token (this might require changes in JwtTokenProvider to pass specific exception type)
        // For now, we'll assume if it's an AuthenticationException and not specifically handled, it's invalid.
        // In a real scenario, JwtTokenProvider's validateToken method would throw specific exceptions
        // that could be caught here to set the correct error code (2003 for expired).

        new ObjectMapper().writeValue(response.getWriter(), errorResponse);
    }
}
