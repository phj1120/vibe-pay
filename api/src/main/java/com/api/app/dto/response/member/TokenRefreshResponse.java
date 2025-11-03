package com.api.app.dto.response.member;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 토큰 갱신 응답 DTO
 *
 * @author system
 * @version 1.0
 * @since 2025-11-03
 */
@Getter
@AllArgsConstructor
public class TokenRefreshResponse {

    private String accessToken;
    private String refreshToken;
}

