package com.vibe.pay.domain.payment.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 결제 취소/환불 응답 DTO
 *
 * PG사로부터 받은 결제 취소/환불 응답 데이터를 담는 객체입니다.
 *
 * Technical Specification 참조:
 * - docs/v5/TechnicalSpecification/payment-and-pg-integration-spec.md
 */
@Data
public class PaymentCancelResponse {

    /**
     * 취소 성공 여부
     */
    private boolean success;

    /**
     * 취소 거래 ID (PG사에서 발급)
     */
    private String cancelTransactionId;

    /**
     * 취소 승인 번호
     */
    private String cancelApprovalNumber;

    /**
     * 취소 금액
     */
    private BigDecimal cancelAmount;

    /**
     * 응답 메시지
     */
    private String message;

    /**
     * 응답 코드
     */
    private String resultCode;

    /**
     * 원본 거래 ID
     */
    private String originalTransactionId;
}
