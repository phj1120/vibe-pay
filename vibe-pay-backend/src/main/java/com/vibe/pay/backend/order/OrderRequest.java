package com.vibe.pay.backend.order;

import java.util.List;

public class OrderRequest {
    private String orderNumber; // 1단계에서 채번된 주문번호
    private Long memberId;
    private List<OrderItemRequest> items;

    
    // 결제 정보 (2단계에서 받은 결제 응답 정보)
    private String authToken;
    private String authUrl;
    private String price;
    private String mid;
    private String netCancelUrl;
    
    // 결제 방법 정보
    private String paymentMethod;
    private Double usedMileage;

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

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
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

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Double getUsedMileage() {
        return usedMileage;
    }

    public void setUsedMileage(Double usedMileage) {
        this.usedMileage = usedMileage;
    }
}
