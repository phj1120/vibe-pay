package com.vibe.pay.backend.payment.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
public class PaymentInitResponse {
    private boolean success;
    private String paymentUrl;
    private String paymentParams;
    private String errorMessage;
    private String paymentId;
    
    // Frontend compatibility fields (matching popup.vue form)
    private String mid;           // merchantId
    private String oid;           // orderId  
    private Long price;           // amount
    private String goodName;      // productName
    private String moId;          // merchantId (same as mid)
    
    // Standard fields
    private String merchantId;
    private String orderId;
    private String amount;
    private String productName;
    private String buyerName;
    private String buyerTel;
    private String buyerEmail;
    private String timestamp;
    private String mKey;
    private String signature;
    private String verification;
    private String returnUrl;
    private String closeUrl;
    private String version;
    private String currency;
    private String gopaymethod;
    private String acceptmethod;


}