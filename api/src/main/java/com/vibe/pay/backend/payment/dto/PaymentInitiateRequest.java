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
public class PaymentInitiateRequest {
    private Long memberId;
    private String orderId;
    private Double amount;
    private String paymentMethod;
    private String pgCompany;
    private Double usedMileage;
    private String goodName;
    private String buyerName;
    private String buyerTel;
    private String buyerEmail;
}
