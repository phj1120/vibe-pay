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
    private String oid;
    private String orderNumber; // 이니시스에서 전달되는 실제 필드명
    private String price;

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

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
