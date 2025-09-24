package com.vibe.pay.backend.payment.gateway;

public class PaymentCancelRequest {
    private String transactionId;
    private String orderId;
    private String amount;
    private String reason;

    // 기본 생성자
    public PaymentCancelRequest() {}

    public PaymentCancelRequest(String transactionId, String orderId, String amount, String reason) {
        this.transactionId = transactionId;
        this.orderId = orderId;
        this.amount = amount;
        this.reason = reason;
    }

    // Getters and Setters
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}