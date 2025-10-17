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

    /**
     * PG사 거래 ID (취소 요청에 사용)
     * originalTransactionId와 동일하지만 setter 지원을 위한 별도 필드
     */
    private String transactionId;

    /**
     * 취소 사유 (cancelReason의 별칭)
     */
    private String reason;

    /**
     * cancelReason의 getter 별칭
     * PaymentService에서 getReason() 호출을 지원하기 위함
     */
    public String getReason() {
        return this.reason != null ? this.reason : this.cancelReason;
    }

    /**
     * cancelReason의 setter 별칭
     * PaymentService에서 setReason() 호출을 지원하기 위함
     */
    public void setReason(String reason) {
        this.reason = reason;
        this.cancelReason = reason;
    }

    /**
     * transactionId getter
     * originalTransactionId를 우선 반환
     */
    public String getTransactionId() {
        return this.transactionId != null ? this.transactionId : this.originalTransactionId;
    }

    /**
     * transactionId setter
     * originalTransactionId도 함께 설정
     */
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
        this.originalTransactionId = transactionId;
    }
}