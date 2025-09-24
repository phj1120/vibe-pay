package com.vibe.pay.backend.payment.gateway;

import com.vibe.pay.backend.payment.PaymentInitiateRequest;
import com.vibe.pay.backend.payment.PaymentConfirmRequest;
import com.vibe.pay.backend.payment.dto.*;  // Import all DTO classes

public interface PaymentGatewayAdapter {
    PaymentInitResponse initiate(PaymentInitiateRequest request);
    PaymentConfirmResponse confirm(PaymentConfirmRequest request);
    void cancel(PaymentCancelRequest request);
    void netCancel(PaymentNetCancelRequest request);
    boolean supports(String pgCompany);
}