package com.vibe.pay.common.exception;

import com.vibe.pay.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 전역 예외 처리 핸들러
 *
 * 애플리케이션 전체에서 발생하는 예외를 처리하고
 * 일관된 형식의 오류 응답을 반환합니다.
 *
 * @author system
 * @version 1.0
 * @since 2025-10-16
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * BusinessException 처리
     *
     * 비즈니스 로직에서 발생하는 예외를 처리합니다.
     *
     * @param ex BusinessException
     * @return ResponseEntity
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        log.warn("Business exception: code={}, message={}", errorCode.getCode(), ex.getMessage());

        return ResponseEntity
            .status(errorCode.getHttpStatus())
            .body(ApiResponse.error(errorCode, ex.getMessage()));
    }

    /**
     * PaymentException 처리
     *
     * 결제 관련 예외를 처리합니다.
     * PG사 연동 오류 정보를 포함하여 로깅합니다.
     *
     * @param ex PaymentException
     * @return ResponseEntity
     */
    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handlePaymentException(
        PaymentException ex
    ) {
        ErrorCode errorCode = ex.getErrorCode();

        // PG사 연동 오류는 에러 레벨로 로깅
        log.error(
            "Payment exception: code={}, message={}, pgCompany={}, pgTransactionId={}, pgErrorCode={}, pgErrorMessage={}",
            errorCode.getCode(),
            ex.getMessage(),
            ex.getPgCompany(),
            ex.getPgTransactionId(),
            ex.getPgErrorCode(),
            ex.getPgErrorMessage(),
            ex
        );

        // PG사 에러 정보가 있으면 응답에 포함
        Map<String, String> pgErrorInfo = null;
        if (ex.getPgCompany() != null || ex.getPgTransactionId() != null) {
            pgErrorInfo = new HashMap<>();
            if (ex.getPgCompany() != null) {
                pgErrorInfo.put("pgCompany", ex.getPgCompany());
            }
            if (ex.getPgTransactionId() != null) {
                pgErrorInfo.put("pgTransactionId", ex.getPgTransactionId());
            }
            if (ex.getPgErrorCode() != null) {
                pgErrorInfo.put("pgErrorCode", ex.getPgErrorCode());
            }
        }

        return ResponseEntity
            .status(errorCode.getHttpStatus())
            .body(ApiResponse.error(errorCode, ex.getMessage(), pgErrorInfo));
    }

    /**
     * ValidationException 처리
     *
     * 비즈니스 로직 검증 실패를 처리합니다.
     * 필드별 상세 검증 오류 정보를 포함합니다.
     *
     * @param ex ValidationException
     * @return ResponseEntity
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
        ValidationException ex
    ) {
        ErrorCode errorCode = ex.getErrorCode();

        log.warn("Validation exception: code={}, fieldErrors={}", errorCode.getCode(), ex.getFieldErrors());

        return ResponseEntity
            .status(errorCode.getHttpStatus())
            .body(ApiResponse.error(errorCode, ex.getMessage(), ex.getFieldErrors()));
    }

    /**
     * MethodArgumentNotValidException 처리
     *
     * @Valid 어노테이션으로 검증 실패 시 발생합니다.
     * 필드별 검증 오류 정보를 포함합니다.
     *
     * @param ex MethodArgumentNotValidException
     * @return ResponseEntity
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleMethodArgumentNotValidException(
        MethodArgumentNotValidException ex
    ) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Method argument not valid: {}", errors);

        return ResponseEntity
            .status(ErrorCode.INVALID_INPUT.getHttpStatus())
            .body(ApiResponse.error(ErrorCode.INVALID_INPUT, errors));
    }

    /**
     * IllegalArgumentException 처리
     *
     * 잘못된 인자 전달 시 발생합니다.
     *
     * @param ex IllegalArgumentException
     * @return ResponseEntity
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
        IllegalArgumentException ex
    ) {
        log.warn("Illegal argument: {}", ex.getMessage());

        return ResponseEntity
            .status(ErrorCode.INVALID_INPUT.getHttpStatus())
            .body(ApiResponse.error(ErrorCode.INVALID_INPUT, ex.getMessage()));
    }

    /**
     * IllegalStateException 처리
     *
     * 잘못된 상태에서 작업 시도 시 발생합니다.
     *
     * @param ex IllegalStateException
     * @return ResponseEntity
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalStateException(
        IllegalStateException ex
    ) {
        log.warn("Illegal state: {}", ex.getMessage());

        return ResponseEntity
            .status(ErrorCode.INVALID_INPUT.getHttpStatus())
            .body(ApiResponse.error(ErrorCode.INVALID_INPUT, ex.getMessage()));
    }

    /**
     * RuntimeException 처리
     *
     * 처리되지 않은 런타임 예외를 처리합니다.
     *
     * @param ex RuntimeException
     * @return ResponseEntity
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime exception occurred", ex);

        return ResponseEntity
            .status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
            .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
    }

    /**
     * Exception 처리 (최종 fallback)
     *
     * 모든 예외의 최종 처리입니다.
     *
     * @param ex Exception
     * @return ResponseEntity
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
        log.error("Unexpected exception occurred", ex);

        return ResponseEntity
            .status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
            .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}
