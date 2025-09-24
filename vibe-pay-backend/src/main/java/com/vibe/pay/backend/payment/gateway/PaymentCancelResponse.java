package com.vibe.pay.backend.payment.gateway;

import java.util.function.LongFunction;

public class PaymentCancelResponse {
    private boolean success;
    private String transactionId;
    private Long cancelAmount;
    private String status;
    private String errorMessage;

    // 기본 생성자
    public PaymentCancelResponse() {}

    public PaymentCancelResponse(boolean success, String transactionId, Long cancelAmount, String status) {
        this.success = success;
        this.transactionId = transactionId;
        this.cancelAmount = cancelAmount;
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

    public Long getCancelAmount() {
        return cancelAmount;
    }

    public void setCancelAmount(Long cancelAmount) {
        this.cancelAmount = cancelAmount;
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