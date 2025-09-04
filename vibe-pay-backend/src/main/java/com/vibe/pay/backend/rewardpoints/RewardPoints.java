package com.vibe.pay.backend.rewardpoints;

import java.time.LocalDateTime;

public class RewardPoints {

    private Long id;
    private Long memberId; // Foreign key to Member
    private Double points;
    private LocalDateTime lastUpdated;

    // Constructors
    public RewardPoints() {
    }

    public RewardPoints(Long memberId, Double points) {
        this.memberId = memberId;
        this.points = points;
        this.lastUpdated = LocalDateTime.now();
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

    public Double getPoints() {
        return points;
    }

    public void setPoints(Double points) {
        this.points = points;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
