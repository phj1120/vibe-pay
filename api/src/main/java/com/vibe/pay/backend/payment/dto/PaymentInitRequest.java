package com.vibe.pay.backend.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 결제 초기화 요청 DTO
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
public class PaymentInitRequest {
    private Long memberId;
    private String orderId;
    private Double amount;
    private String paymentMethod; // CREDIT_CARD, POINT
    private String pgCompany; // INICIS, NICEPAY, TOSS, WEIGHTED
    private Double usedMileage;
    private String goodName;
    private String buyerName;
    private String buyerTel;
    private String buyerEmail;
}
