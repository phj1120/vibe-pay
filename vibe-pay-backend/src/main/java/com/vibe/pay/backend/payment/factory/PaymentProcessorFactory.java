package com.vibe.pay.backend.payment.factory;

import com.vibe.pay.backend.payment.processor.PaymentProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaymentProcessorFactory {

    private final List<PaymentProcessor> processors;

    @Autowired
    public PaymentProcessorFactory(List<PaymentProcessor> processors) {
        this.processors = processors;
    }

    public PaymentProcessor getProcessor(String paymentMethod) {
        return processors.stream()
            .filter(processor -> processor.canProcess(paymentMethod))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No processor found for payment method: " + paymentMethod));
    }
}