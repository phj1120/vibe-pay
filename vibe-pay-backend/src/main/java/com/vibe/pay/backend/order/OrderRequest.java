package com.vibe.pay.backend.order;

import java.util.List;

public class OrderRequest {
    private Long memberId;
    private List<OrderItemRequest> items;
    private Double usedPoints;
    private Long paymentId; // 결제 ID 추가

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }

    public Double getUsedPoints() {
        return usedPoints;
    }

    public void setUsedPoints(Double usedPoints) {
        this.usedPoints = usedPoints;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }
}
