package com.vibe.pay.backend.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 결제 승인 요청 DTO
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
public class PaymentConfirmRequest {
    // 공통
    private Long memberId;
    private String orderId;
    private Double price;
    private String paymentMethod; // CREDIT_CARD, POINT
    private String pgCompany; // INICIS, NICEPAY, TOSS

    // INICIS
    private String authToken;
    private String authUrl;
    private String nextAppUrl;
    private String mid;
    private String netCancelUrl;
    private String txTid;

    // NICEPAY
    private String resultCode;
    private String resultMsg;
    private String tid;
    private String authCode;
    private String cardCode;
    private String cardName;

    // POINT
    private Double pointAmount;
}
