package com.vibe.pay.domain.payment.dto;

import com.vibe.pay.enums.PaymentMethod;
import com.vibe.pay.enums.PgCompany;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 결제 초기화 요청 DTO
 *
 * PG사 결제창 호출을 위한 초기화 요청 데이터 전송 객체입니다.
 *
 * Technical Specification 참조:
 * - docs/v5/TechnicalSpecification/payment-and-pg-integration-spec.md
 */
@Data
public class PaymentInitiateRequest {

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
     * 상품명
     */
    private String productName;

    /**
     * 결제 수단
     */
    private PaymentMethod paymentMethod;

    /**
     * PG사
     */
    private PgCompany pgCompany;

    /**
     * 구매자 이름
     */
    private String buyerName;

    /**
     * 구매자 이메일
     */
    private String buyerEmail;

    /**
     * 구매자 전화번호
     */
    private String buyerPhone;

    /**
     * 결제 완료 후 리턴 URL
     */
    private String returnUrl;

    /**
     * 결제 취소 시 리턴 URL
     */
    private String cancelUrl;
}
