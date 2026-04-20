package com.vibepay.core.dto.response.point

data class PointHistoryListResponse(
    val content: List<PointHistoryResponse> = emptyList(),
    val page: Int = 0,
    val size: Int = 0,
    val totalElements: Long = 0,
    val totalPages: Int = 0
)
