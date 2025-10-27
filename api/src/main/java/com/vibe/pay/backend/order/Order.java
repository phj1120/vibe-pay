package com.vibe.pay.backend.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private String orderId;
    private Integer ordSeq;
    private Integer ordProcSeq;
    private String claimId;
    private Long memberId;
    private LocalDateTime orderDate;
    private Double totalAmount;
    private String status;
}
