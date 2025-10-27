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
public class PaymentNetCancelRequest {
    private String orderNumber;
    private String authToken;
    private String netCancelUrl;
    private String paymentMethod;
    private String pgCompany;
    private String tid;
    private Double amount;
}
