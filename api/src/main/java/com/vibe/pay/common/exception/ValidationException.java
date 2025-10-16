package com.vibe.pay.common.exception;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 입력값 검증 예외
 *
 * 비즈니스 로직에서 발생하는 입력값 검증 오류를 처리합니다.
 * 필드별 상세 검증 오류 정보를 포함할 수 있습니다.
 *
 * @author system
 * @version 1.0
 * @since 2025-10-16
 */
@Getter
public class ValidationException extends BusinessException {

    private final Map<String, String> fieldErrors;

    /**
     * ErrorCode로 검증 예외 생성
     *
     * @param errorCode 에러 코드
     */
    public ValidationException(ErrorCode errorCode) {
        super(errorCode);
        this.fieldErrors = new HashMap<>();
    }

    /**
     * ErrorCode와 상세 메시지로 검증 예외 생성
     *
     * @param errorCode 에러 코드
     * @param detailMessage 상세 메시지
     */
    public ValidationException(ErrorCode errorCode, String detailMessage) {
        super(errorCode, detailMessage);
        this.fieldErrors = new HashMap<>();
    }

    /**
     * ErrorCode와 필드별 에러 정보로 검증 예외 생성
     *
     * @param errorCode 에러 코드
     * @param fieldErrors 필드별 에러 정보 (필드명 -> 에러 메시지)
     */
    public ValidationException(ErrorCode errorCode, Map<String, String> fieldErrors) {
        super(errorCode);
        this.fieldErrors = fieldErrors != null ? new HashMap<>(fieldErrors) : new HashMap<>();
    }

    /**
     * ErrorCode, 상세 메시지, 필드별 에러 정보로 검증 예외 생성
     *
     * @param errorCode 에러 코드
     * @param detailMessage 상세 메시지
     * @param fieldErrors 필드별 에러 정보 (필드명 -> 에러 메시지)
     */
    public ValidationException(
        ErrorCode errorCode,
        String detailMessage,
        Map<String, String> fieldErrors
    ) {
        super(errorCode, detailMessage);
        this.fieldErrors = fieldErrors != null ? new HashMap<>(fieldErrors) : new HashMap<>();
    }

    /**
     * 필드 에러 추가
     *
     * @param fieldName 필드명
     * @param errorMessage 에러 메시지
     */
    public void addFieldError(String fieldName, String errorMessage) {
        this.fieldErrors.put(fieldName, errorMessage);
    }

    /**
     * 필드 에러가 있는지 확인
     *
     * @return 필드 에러 존재 여부
     */
    public boolean hasFieldErrors() {
        return !this.fieldErrors.isEmpty();
    }
}
