package com.vibe.pay.backend.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 결제 초기화 응답 DTO
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
public class PaymentInitResponse {
    private Boolean success;
    private String paymentId;
    private String selectedPgCompany;

    // INICIS 파라미터
    private String mid;
    private String timestamp;
    private String oid;
    private String price;
    private String goodName;
    private String signature;
    private String returnUrl;
    private String closeUrl;

    // NICEPAY 파라미터
    private String merchantKey;
    private String goodsName;
    private String amt;
    private String moid;
    private String buyerName;
    private String buyerTel;
    private String buyerEmail;
    private String cancelUrl;

    // 공통
    private String errorMessage;
}
