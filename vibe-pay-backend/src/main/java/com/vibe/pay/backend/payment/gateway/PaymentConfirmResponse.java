package com.vibe.pay.backend.payment.gateway;

public class PaymentConfirmResponse {
    private boolean success;
    private String transactionId;
    private String amount;
    private String status;
    private String errorMessage;

    // 기본 생성자
    public PaymentConfirmResponse() {}

    public PaymentConfirmResponse(boolean success, String transactionId, String amount, String status) {
        this.success = success;
        this.transactionId = transactionId;
        this.amount = amount;
        this.status = status;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}