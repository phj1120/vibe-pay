package com.vibe.pay.backend.order;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodRequest {
    private String paymentMethod; // CREDIT_CARD, POINT
    private Long amount; // 해당 결제 수단으로 결제할 금액

    // 결제 정보 (2단계에서 받은 결제 응답 정보)
    private String authToken;
    private String authUrl;
    private String mid;
    private String netCancelUrl;
    private String pgCompany; // PG사 정보
    private String txTid;
    private String nextAppUrl;
}