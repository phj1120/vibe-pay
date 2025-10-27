package com.vibe.pay.backend.rewardpoints;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
@Getter
@Setter
public class RewardPointsResponse {
    private Long rewardPointsId;
    private Long memberId;
    private Double points;
    private LocalDateTime lastUpdated;
}
