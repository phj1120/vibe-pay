package com.api.app.common.exception

class ApiException(
    val apiError: ApiError,
    message: String = apiError.message
) : RuntimeException(message)
