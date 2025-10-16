package com.vibe.pay.domain.payment.dto;

import com.vibe.pay.enums.PgCompany;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 결제 취소/환불 요청 DTO
 *
 * PG사를 통한 결제 취소 및 환불 시 사용되는 요청 데이터 전송 객체입니다.
 *
 * Technical Specification 참조:
 * - docs/v5/TechnicalSpecification/payment-and-pg-integration-spec.md
 */
@Data
public class PaymentCancelRequest {

    /**
     * 원본 주문 ID
     */
    private String orderId;

    /**
     * 원본 결제 ID
     */
    private String paymentId;

    /**
     * 취소 금액
     */
    private BigDecimal amount;

    /**
     * PG사
     */
    private PgCompany pgCompany;

    /**
     * 원본 PG사 거래 ID
     */
    private String originalTransactionId;

    /**
     * 원본 승인 번호
     */
    private String originalApprovalNumber;

    /**
     * 취소 사유
     */
    private String cancelReason;

    /**
     * 클레임 ID (주문 취소 시)
     */
    private String claimId;
}