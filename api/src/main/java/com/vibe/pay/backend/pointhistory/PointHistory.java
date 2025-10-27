package com.vibe.pay.backend.pointhistory;

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
public class PointHistory {
    private Long pointHistoryId;
    private Long memberId;
    private Double pointAmount;
    private Double balanceAfter;
    private String transactionType;
    private String referenceType;
    private String referenceId;
    private String description;
    private LocalDateTime createdAt;
}
