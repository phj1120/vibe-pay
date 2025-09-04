package com.vibe.pay.backend.rewardpoints;

public class RewardPointsRequest {
    private Long memberId;
    private Double points;

    // Getters and Setters
    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public Double getPoints() {
        return points;
    }

    public void setPoints(Double points) {
        this.points = points;
    }
}
