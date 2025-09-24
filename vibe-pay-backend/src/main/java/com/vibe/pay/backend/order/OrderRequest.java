package com.vibe.pay.backend.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    private String orderNumber; // 1단계에서 채번된 주문번호
    private Long memberId;
    private List<OrderItemRequest> items;
    private List<PaymentMethodRequest> paymentMethods;

    private boolean netCancel;
}
