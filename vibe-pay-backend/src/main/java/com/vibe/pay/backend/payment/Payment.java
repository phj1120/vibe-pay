package com.vibe.pay.backend.payment;

import java.time.LocalDateTime;

public class Payment {

    private String paymentId;
    private Long memberId;
    private String orderId;
    private String claimId;
    private Double amount;
    private Double usedPoints; // 사용된 포인트
    private String paymentMethod;
    private String payType; // PAYMENT(결제), REFUND(환불)
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
        this.usedPoints = 0.0; // 기본값 0
        this.paymentMethod = paymentMethod;
        this.payType = "PAYMENT"; // 기본값 PAYMENT
        this.pgCompany = pgCompany;
        this.status = status;
        this.transactionId = transactionId;
        this.paymentDate = LocalDateTime.now();
    }

    // 포인트 사용 정보 포함 생성자
    public Payment(Long memberId, String orderId, String claimId, Double amount, Double usedPoints, String paymentMethod, String pgCompany, String status, String transactionId) {
        this.memberId = memberId;
        this.orderId = orderId;
        this.claimId = claimId;
        this.amount = amount;
        this.usedPoints = usedPoints;
        this.paymentMethod = paymentMethod;
        this.payType = "PAYMENT"; // 기본값 PAYMENT
        this.pgCompany = pgCompany;
        this.status = status;
        this.transactionId = transactionId;
        this.paymentDate = LocalDateTime.now();
    }

    // 완전한 생성자 (payType 포함)
    public Payment(Long memberId, String orderId, String claimId, Double amount, Double usedPoints,
                   String paymentMethod, String payType, String pgCompany, String status, String transactionId) {
        this.memberId = memberId;
        this.orderId = orderId;
        this.claimId = claimId;
        this.amount = amount;
        this.usedPoints = usedPoints;
        this.paymentMethod = paymentMethod;
        this.payType = payType;
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

    public Double getUsedPoints() {
        return usedPoints;
    }

    public void setUsedPoints(Double usedPoints) {
        this.usedPoints = usedPoints;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
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
