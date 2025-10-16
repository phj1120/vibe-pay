package com.vibe.pay.domain.payment.dto;

import com.vibe.pay.enums.PaymentMethod;
import com.vibe.pay.enums.PgCompany;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 결제 승인 요청 DTO
 *
 * PG사를 통한 결제 승인 시 사용되는 요청 데이터 전송 객체입니다.
 *
 * Technical Specification 참조:
 * - docs/v5/TechnicalSpecification/payment-and-pg-integration-spec.md
 */
@Data
public class PaymentConfirmRequest {

    /**
     * 주문 ID
     */
    private String orderId;

    /**
     * 회원 ID
     */
    private Long memberId;

    /**
     * 결제 금액
     */
    private BigDecimal amount;

    /**
     * 결제 수단
     */
    private PaymentMethod paymentMethod;

    /**
     * PG사
     */
    private PgCompany pgCompany;

    /**
     * PG사 거래 ID (PG사에서 발급한 거래 식별자)
     */
    private String pgTransactionId;

    /**
     * 카드 번호 (마스킹된)
     */
    private String cardNumber;

    /**
     * 카드 승인 번호
     */
    private String approvalNumber;
}