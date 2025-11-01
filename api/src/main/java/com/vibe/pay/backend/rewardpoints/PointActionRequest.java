package com.vibe.pay.backend.rewardpoints;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PointActionRequest {
    private Double pointAmount;
    private String description;
}
