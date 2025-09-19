package com.vibe.pay.backend.payment;

import java.time.LocalDateTime;

public class Payment {

    private String paymentId;
    private Long memberId;
    private String orderId;
    private String claimId;
    private Double amount;
    private String paymentMethod;
    private String pgCompany;
    private String status;
    private String transactionId;
    private LocalDateTime paymentDate;

    // Constructors
    public Payment() {
    }

    public Payment(Long memberId, String orderId, String claimId, Double amount, String paymentMethod, String pgCompany, String status, String transactionId) {
        this.memberId = memberId;
        this.orderId = orderId;
        this.claimId = claimId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.pgCompany = pgCompany;
        this.status = status;
        this.transactionId = transactionId;
        this.paymentDate = LocalDateTime.now();
    }

    // Getters and Setters
    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getClaimId() {
        return claimId;
    }

    public void setClaimId(String claimId) {
        this.claimId = claimId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPgCompany() {
        return pgCompany;
    }

    public void setPgCompany(String pgCompany) {
        this.pgCompany = pgCompany;
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

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }
}
