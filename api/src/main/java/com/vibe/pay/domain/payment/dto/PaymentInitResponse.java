package com.vibe.pay.domain.payment.dto;

import lombok.Data;

import java.util.Map;

/**
 * 결제 초기화 응답 DTO
 *
 * PG사 결제창 호출을 위한 초기화 응답 데이터를 담는 전송 객체입니다.
 *
 * Technical Specification 참조:
 * - docs/v5/TechnicalSpecification/payment-and-pg-integration-spec.md
 */
@Data
public class PaymentInitResponse {

    /**
     * 초기화 성공 여부
     */
    private boolean success;

    /**
     * 결제창 URL
     */
    private String paymentUrl;

    /**
     * 결제창 호출 시 필요한 파라미터
     * (PG사별로 다른 파라미터들을 Map으로 관리)
     */
    private Map<String, String> parameters;

    /**
     * 응답 메시지
     */
    private String message;

    /**
     * 에러 코드 (실패 시)
     */
    private String errorCode;
}
