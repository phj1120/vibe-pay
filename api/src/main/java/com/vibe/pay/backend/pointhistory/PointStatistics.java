package com.vibe.pay.backend.pointhistory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointStatistics {
    private Double totalEarned;
    private Double totalUsed;
    private Double totalRefunded;
    private Double currentBalance;
}
