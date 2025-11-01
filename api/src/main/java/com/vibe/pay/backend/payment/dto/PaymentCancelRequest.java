package com.vibe.pay.backend.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 결제 취소 요청 DTO
 *
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCancelRequest {
    private String paymentId;
    private String orderId;
    private String pgCompany;
    private String transactionId;
    private Double amount;
    private String reason;
}
