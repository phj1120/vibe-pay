package com.vibe.pay.domain.payment.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 결제 승인 응답 DTO
 *
 * PG사로부터의 결제 승인 응답 데이터를 담는 전송 객체입니다.
 *
 * Technical Specification 참조:
 * - docs/v5/TechnicalSpecification/payment-and-pg-integration-spec.md
 */
@Data
public class PaymentConfirmResponse {

    /**
     * 승인 성공 여부
     */
    private boolean success;

    /**
     * PG사 거래 ID
     */
    private String transactionId;

    /**
     * 승인 번호
     */
    private String approvalNumber;

    /**
     * 승인 금액
     */
    private BigDecimal amount;

    /**
     * 응답 메시지
     */
    private String message;

    /**
     * PG사 응답 코드
     */
    private String resultCode;

    /**
     * 카드 번호 (마스킹된)
     */
    private String maskedCardNumber;

    /**
     * 카드사 이름
     */
    private String cardCompany;
}