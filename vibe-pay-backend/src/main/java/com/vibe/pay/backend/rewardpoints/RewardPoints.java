package com.vibe.pay.backend.rewardpoints;

import java.time.LocalDateTime;

public class RewardPoints {

    private Long rewardPointsId;
    private Long memberId; // Foreign key to Member
    private Long points;
    private LocalDateTime lastUpdated;

    // Constructors
    public RewardPoints() {
    }

    public RewardPoints(Long memberId, Long points) {
        this.memberId = memberId;
        this.points = points;
        this.lastUpdated = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getRewardPointsId() {
        return rewardPointsId;
    }

    public void setRewardPointsId(Long rewardPointsId) {
        this.rewardPointsId = rewardPointsId;
    }

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

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
