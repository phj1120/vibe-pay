package com.vibe.pay.backend.pointhistory;

import java.time.LocalDateTime;

public class PointHistory {
    private Long pointHistoryId;
    private Long memberId;
    private Double pointAmount; // 포인트 변동량 (+ 적립, - 사용)
    private Double balanceAfter; // 변동 후 잔액
    private String transactionType; // EARN(적립), USE(사용), REFUND(환불)
    private String referenceType; // PAYMENT(결제), CANCEL(취소), MANUAL(수동)
    private String referenceId; // 연관된 ID (payment_id, order_id 등)
    private String description; // 설명
    private LocalDateTime createdAt;

    // 기본 생성자
    public PointHistory() {}

    // 생성자 (주요 필드)
    public PointHistory(Long memberId, Double pointAmount, Double balanceAfter,
                       String transactionType, String referenceType, String referenceId, String description) {
        this.memberId = memberId;
        this.pointAmount = pointAmount;
        this.balanceAfter = balanceAfter;
        this.transactionType = transactionType;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getPointHistoryId() {
        return pointHistoryId;
    }

    public void setPointHistoryId(Long pointHistoryId) {
        this.pointHistoryId = pointHistoryId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public Double getPointAmount() {
        return pointAmount;
    }

    public void setPointAmount(Double pointAmount) {
        this.pointAmount = pointAmount;
    }

    public Double getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(Double balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "PointHistory{" +
                "pointHistoryId=" + pointHistoryId +
                ", memberId=" + memberId +
                ", pointAmount=" + pointAmount +
                ", balanceAfter=" + balanceAfter +
                ", transactionType='" + transactionType + '\'' +
                ", referenceType='" + referenceType + '\'' +
                ", referenceId='" + referenceId + '\'' +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}