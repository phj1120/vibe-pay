package com.vibe.pay.backend.pointhistory;

public class PointStatistics {
    private Long memberId;
    private Double currentBalance; // 현재 포인트 잔액
    private Double totalEarned; // 총 적립 포인트
    private Double totalUsed; // 총 사용 포인트
    private Double totalRefunded; // 총 환불 포인트
    private Integer earnCount; // 적립 횟수
    private Integer useCount; // 사용 횟수
    private Integer refundCount; // 환불 횟수
    private Integer totalTransactions; // 총 거래 횟수

    // 기본 생성자
    public PointStatistics() {}

    // 전체 생성자
    public PointStatistics(Long memberId, Double currentBalance, Double totalEarned, Double totalUsed,
                          Double totalRefunded, Integer earnCount, Integer useCount, Integer refundCount,
                          Integer totalTransactions) {
        this.memberId = memberId;
        this.currentBalance = currentBalance;
        this.totalEarned = totalEarned;
        this.totalUsed = totalUsed;
        this.totalRefunded = totalRefunded;
        this.earnCount = earnCount;
        this.useCount = useCount;
        this.refundCount = refundCount;
        this.totalTransactions = totalTransactions;
    }

    // Getters and Setters
    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public Double getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(Double currentBalance) {
        this.currentBalance = currentBalance;
    }

    public Double getTotalEarned() {
        return totalEarned;
    }

    public void setTotalEarned(Double totalEarned) {
        this.totalEarned = totalEarned;
    }

    public Double getTotalUsed() {
        return totalUsed;
    }

    public void setTotalUsed(Double totalUsed) {
        this.totalUsed = totalUsed;
    }

    public Double getTotalRefunded() {
        return totalRefunded;
    }

    public void setTotalRefunded(Double totalRefunded) {
        this.totalRefunded = totalRefunded;
    }

    public Integer getEarnCount() {
        return earnCount;
    }

    public void setEarnCount(Integer earnCount) {
        this.earnCount = earnCount;
    }

    public Integer getUseCount() {
        return useCount;
    }

    public void setUseCount(Integer useCount) {
        this.useCount = useCount;
    }

    public Integer getRefundCount() {
        return refundCount;
    }

    public void setRefundCount(Integer refundCount) {
        this.refundCount = refundCount;
    }

    public Integer getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(Integer totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    @Override
    public String toString() {
        return "PointStatistics{" +
                "memberId=" + memberId +
                ", currentBalance=" + currentBalance +
                ", totalEarned=" + totalEarned +
                ", totalUsed=" + totalUsed +
                ", totalRefunded=" + totalRefunded +
                ", earnCount=" + earnCount +
                ", useCount=" + useCount +
                ", refundCount=" + refundCount +
                ", totalTransactions=" + totalTransactions +
                '}';
    }
}