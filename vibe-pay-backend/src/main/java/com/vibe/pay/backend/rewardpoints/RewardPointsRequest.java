package com.vibe.pay.backend.rewardpoints;

public class RewardPointsRequest {
    private Long memberId;
    private Long points;

    // Getters and Setters
    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public Long getPoints() {
        return points;
    }

    public void setPoints(Long points) {
        this.points = points;
    }
}
