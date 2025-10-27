package com.vibe.pay.backend.payment.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
@Getter
@Setter
public class PaymentInitResponse {
    private boolean success;
    private String paymentId;
    private String selectedPgCompany;
    private String errorMessage;
    // PG사별 파라미터는 Phase 2-4에서 추가 예정
}
