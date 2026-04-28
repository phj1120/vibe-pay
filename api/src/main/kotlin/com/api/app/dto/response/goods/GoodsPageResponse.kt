package com.api.app.dto.response.goods

data class GoodsPageResponse(
    val content: List<GoodsListResponse> = emptyList(),
    val page: Int = 0,
    val size: Int = 0,
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val last: Boolean = false
)
