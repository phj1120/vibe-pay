package com.vibe.pay.backend.order;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
@Getter
@Setter
public class PaymentMethodRequest {
    private String paymentMethod;
    private String pgCompany;
    private Double amount;
    private String authToken;
    private String authUrl;
    private String nextAppUrl;
    private String mid;
    private String netCancelUrl;
    private String txTid;
}
