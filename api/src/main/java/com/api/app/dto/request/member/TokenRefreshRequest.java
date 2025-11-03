package com.api.app.dto.request.member;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 토큰 갱신 요청 DTO
 *
 * @author system
 * @version 1.0
 * @since 2025-11-03
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TokenRefreshRequest {

    @NotBlank(message = "리프레시 토큰은 필수입니다")
    private String refreshToken;
}

