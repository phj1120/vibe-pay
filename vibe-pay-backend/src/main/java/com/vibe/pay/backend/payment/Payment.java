package com.vibe.pay.backend.payment;

import java.time.LocalDateTime;

public class Payment {

    private Long id;
    private Long memberId;
    private Double amount;
    private String paymentMethod;
    private String pgCompany;
    private String status;
    private String transactionId;
    private LocalDateTime paymentDate;

    // Constructors
    public Payment() {
    }

    public Payment(Long memberId, Double amount, String paymentMethod, String pgCompany, String status, String transactionId) {
        this.memberId = memberId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.pgCompany = pgCompany;
        this.status = status;
        this.transactionId = transactionId;
        this.paymentDate = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
