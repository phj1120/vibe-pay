package com.vibe.pay.backend.rewardpoints;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RewardPoints {
    private Long rewardPointsId;
    private Long memberId;
    private Double points;
    private LocalDateTime lastUpdated;
}
