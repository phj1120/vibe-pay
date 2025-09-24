package com.vibe.pay.backend.payment.gateway;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentNetCancelRequest {
    private String netCancelUrl;
    private String authToken;
    private String orderNumber;
}
