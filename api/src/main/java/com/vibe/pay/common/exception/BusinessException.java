package com.vibe.pay.common.exception;

import lombok.Getter;

/**
 * 비즈니스 로직 예외
 *
 * 비즈니스 규칙 위반 시 발생하는 커스텀 예외입니다.
 * ErrorCode를 기반으로 예외를 생성하고 관리합니다.
 *
 * @author system
 * @version 1.0
 * @since 2025-10-16
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String detailMessage;

    /**
     * ErrorCode로 예외 생성
     *
     * @param errorCode 에러 코드
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detailMessage = null;
    }

    /**
     * ErrorCode와 상세 메시지로 예외 생성
     *
     * @param errorCode 에러 코드
     * @param detailMessage 상세 메시지
     */
    public BusinessException(ErrorCode errorCode, String detailMessage) {
        super(errorCode.getMessage() + " - " + detailMessage);
        this.errorCode = errorCode;
        this.detailMessage = detailMessage;
    }

    /**
     * ErrorCode와 원인 예외로 예외 생성
     *
     * @param errorCode 에러 코드
     * @param cause 원인 예외
     */
    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.detailMessage = null;
    }

    /**
     * ErrorCode, 상세 메시지, 원인 예외로 예외 생성
     *
     * @param errorCode 에러 코드
     * @param detailMessage 상세 메시지
     * @param cause 원인 예외
     */
    public BusinessException(ErrorCode errorCode, String detailMessage, Throwable cause) {
        super(errorCode.getMessage() + " - " + detailMessage, cause);
        this.errorCode = errorCode;
        this.detailMessage = detailMessage;
    }
}
