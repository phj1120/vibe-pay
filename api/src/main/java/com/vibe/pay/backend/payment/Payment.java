package com.vibe.pay.backend.payment;

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
public class Payment {
    private String paymentId;
    private Long memberId;
    private String orderId;
    private String claimId;
    private Double amount;
    private String paymentMethod;
    private String payType;
    private String pgCompany;
    private String status;
    private String orderStatus;
    private String transactionId;
    private LocalDateTime paymentDate;
}
