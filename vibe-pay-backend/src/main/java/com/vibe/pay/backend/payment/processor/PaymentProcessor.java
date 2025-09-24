package com.vibe.pay.backend.payment.processor;

import com.vibe.pay.backend.common.Constants;
import com.vibe.pay.backend.payment.Payment;
import com.vibe.pay.backend.payment.PaymentConfirmRequest;
import com.vibe.pay.backend.payment.gateway.PaymentNetCancelRequest;

public interface PaymentProcessor {
    Payment processPayment(PaymentConfirmRequest request);
    Payment processRefund(Payment originalPayment);
    void netCancel(PaymentNetCancelRequest paymentNetCancelRequest);
    boolean canProcess(String paymentMethod);
}