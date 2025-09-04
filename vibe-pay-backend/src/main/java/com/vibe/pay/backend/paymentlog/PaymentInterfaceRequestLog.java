package com.vibe.pay.backend.paymentlog;

import java.time.LocalDateTime;

public class PaymentInterfaceRequestLog {

    private Long id;
    private Long paymentId;
    private String requestType;

    private String requestPayload;

    private String responsePayload;
    private LocalDateTime timestamp;

    // Constructors
    public PaymentInterfaceRequestLog() {
    }

    public PaymentInterfaceRequestLog(Long paymentId, String requestType, String requestPayload, String responsePayload) {
        this.paymentId = paymentId;
        this.requestType = requestType;
        this.requestPayload = requestPayload;
        this.responsePayload = responsePayload;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
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
