package com.vibe.pay.backend.order;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
@Getter
@Setter
public class OrderRequest {
    private String orderNumber;
    private Long memberId;
    private List<OrderItemRequest> items;
    private List<PaymentMethodRequest> paymentMethods;
    private Boolean netCancel; // for testing
}
