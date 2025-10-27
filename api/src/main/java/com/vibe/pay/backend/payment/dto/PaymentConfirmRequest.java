package com.vibe.pay.backend.payment.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
@Getter
@Setter
public class PaymentConfirmRequest {
    private String authToken;
    private String authUrl;
    private String nextAppUrl;
    private String orderId;
    private Double price;
    private String mid;
    private String netCancelUrl;
    private Long memberId;
    private String paymentMethod;
    private String pgCompany;
    private String txTid;
}
