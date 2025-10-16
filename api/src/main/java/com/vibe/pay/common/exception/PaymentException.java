package com.vibe.pay.common.exception;

import lombok.Getter;

/**
 * 결제 관련 예외
 *
 * 결제 프로세스 및 PG사 연동 오류 시 발생하는 특화 예외입니다.
 * 결제 실패, PG사 통신 오류, 결제 금액 불일치 등의 상황을 처리합니다.
 *
 * @author system
 * @version 1.0
 * @since 2025-10-16
 */
@Getter
public class PaymentException extends BusinessException {

    private final String pgCompany;
    private final String pgTransactionId;
    private final String pgErrorCode;
    private final String pgErrorMessage;

    /**
     * ErrorCode로 결제 예외 생성
     *
     * @param errorCode 에러 코드
     */
    public PaymentException(ErrorCode errorCode) {
        super(errorCode);
        this.pgCompany = null;
        this.pgTransactionId = null;
        this.pgErrorCode = null;
        this.pgErrorMessage = null;
    }

    /**
     * ErrorCode와 상세 메시지로 결제 예외 생성
     *
     * @param errorCode 에러 코드
     * @param detailMessage 상세 메시지
     */
    public PaymentException(ErrorCode errorCode, String detailMessage) {
        super(errorCode, detailMessage);
        this.pgCompany = null;
        this.pgTransactionId = null;
        this.pgErrorCode = null;
        this.pgErrorMessage = null;
    }

    /**
     * PG사 정보를 포함한 결제 예외 생성
     *
     * @param errorCode 에러 코드
     * @param pgCompany PG사명
     * @param pgTransactionId PG사 거래 ID
     */
    public PaymentException(ErrorCode errorCode, String pgCompany, String pgTransactionId) {
        super(errorCode);
        this.pgCompany = pgCompany;
        this.pgTransactionId = pgTransactionId;
        this.pgErrorCode = null;
        this.pgErrorMessage = null;
    }

    /**
     * PG사 에러 정보를 포함한 결제 예외 생성
     *
     * @param errorCode 에러 코드
     * @param pgCompany PG사명
     * @param pgTransactionId PG사 거래 ID
     * @param pgErrorCode PG사 에러 코드
     * @param pgErrorMessage PG사 에러 메시지
     */
    public PaymentException(
        ErrorCode errorCode,
        String pgCompany,
        String pgTransactionId,
        String pgErrorCode,
        String pgErrorMessage
    ) {
        super(errorCode, pgErrorMessage);
        this.pgCompany = pgCompany;
        this.pgTransactionId = pgTransactionId;
        this.pgErrorCode = pgErrorCode;
        this.pgErrorMessage = pgErrorMessage;
    }

    /**
     * PG사 에러 정보와 원인 예외를 포함한 결제 예외 생성
     *
     * @param errorCode 에러 코드
     * @param pgCompany PG사명
     * @param pgTransactionId PG사 거래 ID
     * @param pgErrorCode PG사 에러 코드
     * @param pgErrorMessage PG사 에러 메시지
     * @param cause 원인 예외
     */
    public PaymentException(
        ErrorCode errorCode,
        String pgCompany,
        String pgTransactionId,
        String pgErrorCode,
        String pgErrorMessage,
        Throwable cause
    ) {
        super(errorCode, pgErrorMessage, cause);
        this.pgCompany = pgCompany;
        this.pgTransactionId = pgTransactionId;
        this.pgErrorCode = pgErrorCode;
        this.pgErrorMessage = pgErrorMessage;
    }
}
