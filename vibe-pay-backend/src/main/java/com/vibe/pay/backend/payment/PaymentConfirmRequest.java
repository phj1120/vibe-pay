package com.vibe.pay.backend.payment;

public class PaymentConfirmRequest {
    private Long paymentId;
    private String status; // e.g., "SUCCESS", "FAILURE"
    private String transactionId;
    
    // 이니시스 승인 처리용 필드
    private String authToken;
    private String authUrl;
    private String netCancelUrl;
    private String mid;
    private String orderId; // 이니시스에서 전달되는 실제 필드명 (oid와 동일)
    private String price;
    
    // 결제 승인 시 필요한 추가 정보
    private Long memberId;
    private String paymentMethod;

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
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

    public String getNetCancelUrl() {
        return netCancelUrl;
    }

    public void setNetCancelUrl(String netCancelUrl) {
        this.netCancelUrl = netCancelUrl;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
