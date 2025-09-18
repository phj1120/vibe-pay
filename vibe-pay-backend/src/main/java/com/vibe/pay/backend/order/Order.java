package com.vibe.pay.backend.order;

import java.time.LocalDateTime;

public class Order {

    private String id;
    private Long memberId;
    private LocalDateTime orderDate;
    private Double totalAmount;
    private Double usedRewardPoints;
    private Double finalPaymentAmount;
    private String status;
    private String paymentId; // New field

    public Order() {
    }

    public Order(Long memberId, Double totalAmount, Double usedRewardPoints, Double finalPaymentAmount, String status) {
        this.memberId = memberId;
        this.orderDate = LocalDateTime.now();
        this.totalAmount = totalAmount;
        this.usedRewardPoints = usedRewardPoints;
        this.finalPaymentAmount = finalPaymentAmount;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Double getUsedRewardPoints() {
        return usedRewardPoints;
    }

    public void setUsedRewardPoints(Double usedRewardPoints) {
        this.usedRewardPoints = usedRewardPoints;
    }

    public Double getFinalPaymentAmount() {
        return finalPaymentAmount;
    }

    public void setFinalPaymentAmount(Double finalPaymentAmount) {
        this.finalPaymentAmount = finalPaymentAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }
}