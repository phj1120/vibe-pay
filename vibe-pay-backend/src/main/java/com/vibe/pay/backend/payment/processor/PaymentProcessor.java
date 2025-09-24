package com.vibe.pay.backend.payment.processor;

import com.vibe.pay.backend.payment.Payment;
import com.vibe.pay.backend.payment.PaymentConfirmRequest;

public interface PaymentProcessor {
    Payment processPayment(PaymentConfirmRequest request);
    Payment processRefund(Payment originalPayment);
    boolean canProcess(String paymentMethod);
}