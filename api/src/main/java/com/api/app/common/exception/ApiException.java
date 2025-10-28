package com.api.app.common.exception;

import lombok.Getter;

/**
 * @author system
 * @version 1.0
 * @since 2025-10-28
 */
@Getter
public class ApiException extends RuntimeException {

    private final ApiError apiError;

    public ApiException(ApiError apiError) {
        super(apiError.getMessage());
        this.apiError = apiError;
    }

    public ApiException(ApiError apiError, String customMessage) {
        super(customMessage);
        this.apiError = apiError;
    }
}
