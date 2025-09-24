package com.vibe.pay.backend.order;

import java.util.List;

public class OrderRequest {
    private String orderNumber; // 1단계에서 채번된 주문번호
    private Long memberId;
    private List<OrderItemRequest> items;

    
    // 결제 정보 (2단계에서 받은 결제 응답 정보)
    private String authToken;
    private String authUrl;
    private Long price;
    private String mid;
    private String netCancelUrl;
    
    // 결제 방법 정보
    private List<PaymentMethodRequest> paymentMethods;

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



    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getAuthUrl() {
        return authUrl;
    }

    public void setAuthUrl(String authUrl) {
        this.authUrl = authUrl;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getNetCancelUrl() {
        return netCancelUrl;
    }

    public void setNetCancelUrl(String netCancelUrl) {
        this.netCancelUrl = netCancelUrl;
    }

    public List<PaymentMethodRequest> getPaymentMethods() {
        return paymentMethods;
    }

    public void setPaymentMethods(List<PaymentMethodRequest> paymentMethods) {
        this.paymentMethods = paymentMethods;
    }

    // 기존 호환성을 위한 메서드 (첫 번째 결제 수단 반환)
    public String getPaymentMethod() {
        return paymentMethods != null && !paymentMethods.isEmpty()
            ? paymentMethods.get(0).getPaymentMethod()
            : null;
    }
}
