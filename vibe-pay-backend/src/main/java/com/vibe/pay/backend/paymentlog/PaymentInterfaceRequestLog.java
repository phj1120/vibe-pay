package com.vibe.pay.backend.paymentlog;

import java.time.LocalDateTime;

public class PaymentInterfaceRequestLog {

    private Long logId;
    private String paymentId;
    private String requestType;

    private String requestPayload;

    private String responsePayload;
    private LocalDateTime timestamp;

    // Constructors
    public PaymentInterfaceRequestLog() {
    }

    public PaymentInterfaceRequestLog(String paymentId, String requestType, String requestPayload, String responsePayload) {
        this.paymentId = paymentId;
        this.requestType = requestType;
        this.requestPayload = requestPayload;
        this.responsePayload = responsePayload;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getRequestPayload() {
        return requestPayload;
    }

    public void setRequestPayload(String requestPayload) {
        this.requestPayload = requestPayload;
    }

    public String getResponsePayload() {
        return responsePayload;
    }

    public void setResponsePayload(String responsePayload) {
        this.responsePayload = responsePayload;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
