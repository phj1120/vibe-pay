package com.api.app.common.response;

import com.api.app.common.exception.ApiError;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * @author system
 * @version 1.0
 * @since 2025-10-28
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final LocalDateTime timestamp;
    private final String code;
    private final String message;
    private final T data;

    public ApiResponse(String code, String message, T data) {
        this.timestamp = LocalDateTime.now();
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 성공 응답 (데이터 포함)
     *
     * @param data 응답 데이터
     * @param <T>  데이터 타입
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ApiError.SUCCESS.getCode(), ApiError.SUCCESS.getMessage(), data);
    }

    /**
     * 성공 응답 (데이터 없음)
     *
     * @param <T> 데이터 타입
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(ApiError.SUCCESS.getCode(), ApiError.SUCCESS.getMessage(), null);
    }

    /**
     * 실패 응답
     *
     * @param apiError 에러 정보
     * @param <T>      데이터 타입
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(ApiError apiError) {
        return new ApiResponse<>(apiError.getCode(), apiError.getMessage(), null);
    }

    /**
     * 실패 응답 (커스텀 메시지)
     *
     * @param apiError      에러 정보
     * @param customMessage 커스텀 메시지
     * @param <T>           데이터 타입
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(ApiError apiError, String customMessage) {
        return new ApiResponse<>(apiError.getCode(), customMessage, null);
    }
}
