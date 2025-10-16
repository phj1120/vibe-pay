package com.vibe.pay.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vibe.pay.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * API 공통 응답 DTO
 *
 * 모든 API 응답에 사용되는 표준 응답 형식입니다.
 *
 * @param <T> 응답 데이터 타입
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "API 공통 응답")
public class ApiResponse<T> {

    @Schema(description = "성공 여부", example = "true")
    private final boolean success;

    @Schema(description = "응답 메시지", example = "성공")
    private final String message;

    @Schema(description = "에러 코드", example = "1001")
    private final String errorCode;

    @Schema(description = "응답 데이터")
    private final T data;

    @Schema(description = "응답 시간", example = "2024-10-16T10:30:00")
    private final LocalDateTime timestamp;

    /**
     * 생성자
     *
     * @param success 성공 여부
     * @param message 응답 메시지
     * @param errorCode 에러 코드
     * @param data 응답 데이터
     */
    private ApiResponse(boolean success, String message, String errorCode, T data) {
        this.success = success;
        this.message = message;
        this.errorCode = errorCode;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * 성공 응답 (데이터 포함)
     *
     * @param data 응답 데이터
     * @param <T> 데이터 타입
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "성공", null, data);
    }

    /**
     * 성공 응답 (메시지와 데이터 포함)
     *
     * @param message 응답 메시지
     * @param data 응답 데이터
     * @param <T> 데이터 타입
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, null, data);
    }

    /**
     * 성공 응답 (데이터 없음)
     *
     * @param message 응답 메시지
     * @return ApiResponse
     */
    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(true, message, null, null);
    }

    /**
     * 성공 응답 (기본 메시지)
     *
     * @return ApiResponse
     */
    public static ApiResponse<Void> success() {
        return new ApiResponse<>(true, "성공", null, null);
    }

    /**
     * 실패 응답 (메시지만)
     *
     * @param message 오류 메시지
     * @return ApiResponse
     */
    public static ApiResponse<Void> error(String message) {
        return new ApiResponse<>(false, message, null, null);
    }

    /**
     * 실패 응답 (메시지와 데이터)
     *
     * @param message 오류 메시지
     * @param data 오류 데이터 (예: validation errors)
     * @param <T> 데이터 타입
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(false, message, null, data);
    }

    /**
     * 실패 응답 (ErrorCode 기반)
     *
     * @param errorCode 에러 코드
     * @return ApiResponse
     */
    public static ApiResponse<Void> error(ErrorCode errorCode) {
        return new ApiResponse<>(false, errorCode.getMessage(), errorCode.getCode(), null);
    }

    /**
     * 실패 응답 (ErrorCode와 데이터)
     *
     * @param errorCode 에러 코드
     * @param data 오류 데이터
     * @param <T> 데이터 타입
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(ErrorCode errorCode, T data) {
        return new ApiResponse<>(false, errorCode.getMessage(), errorCode.getCode(), data);
    }

    /**
     * 실패 응답 (ErrorCode와 커스텀 메시지)
     *
     * @param errorCode 에러 코드
     * @param customMessage 커스텀 메시지
     * @return ApiResponse
     */
    public static ApiResponse<Void> error(ErrorCode errorCode, String customMessage) {
        return new ApiResponse<>(false, customMessage, errorCode.getCode(), null);
    }

    /**
     * 실패 응답 (ErrorCode, 커스텀 메시지, 데이터)
     *
     * @param errorCode 에러 코드
     * @param customMessage 커스텀 메시지
     * @param data 오류 데이터
     * @param <T> 데이터 타입
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(ErrorCode errorCode, String customMessage, T data) {
        return new ApiResponse<>(false, customMessage, errorCode.getCode(), data);
    }
}
