package com.api.app.common.response

import com.api.app.common.exception.ApiError
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val code: String,
    val message: String,
    val data: T? = null
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> =
            ApiResponse(code = ApiError.SUCCESS.code, message = ApiError.SUCCESS.message, data = data)

        fun <T> success(): ApiResponse<T> =
            ApiResponse(code = ApiError.SUCCESS.code, message = ApiError.SUCCESS.message)

        fun <T> error(apiError: ApiError): ApiResponse<T> =
            ApiResponse(code = apiError.code, message = apiError.message)

        fun <T> error(apiError: ApiError, customMessage: String): ApiResponse<T> =
            ApiResponse(code = apiError.code, message = customMessage)
    }
}
