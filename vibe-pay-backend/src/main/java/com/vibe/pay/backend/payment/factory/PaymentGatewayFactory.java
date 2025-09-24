package com.vibe.pay.backend.payment.factory;

import com.vibe.pay.backend.payment.gateway.PaymentGatewayAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaymentGatewayFactory {

    private final List<PaymentGatewayAdapter> adapters;

    @Autowired
    public PaymentGatewayFactory(List<PaymentGatewayAdapter> adapters) {
        this.adapters = adapters;
    }

    public PaymentGatewayAdapter getAdapter(String pgCompany) {
        return adapters.stream()
            .filter(adapter -> adapter.supports(pgCompany))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No adapter found for PG company: " + pgCompany));
    }
}