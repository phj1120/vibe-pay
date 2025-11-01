package com.vibe.pay.backend.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * INICIS 승인 응답 DTO
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
public class InicisConfirmResponse {
    private String resultCode;
    private String resultMsg;
    private String tid;
    private String oid;
    private String price;
    private String paidAt;
    private String cardCode;
    private String cardName;
    private String cardQuota;
}
