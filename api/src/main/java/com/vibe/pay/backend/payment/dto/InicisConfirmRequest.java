package com.vibe.pay.backend.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * INICIS 승인 요청 DTO
 *
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InicisConfirmRequest {
    private String authToken;
    private String authUrl;
    private String netCancelUrl;
    private String charset;
    private String format;
}
