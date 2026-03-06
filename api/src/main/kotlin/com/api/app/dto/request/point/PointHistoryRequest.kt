package com.api.app.dto.request.point

data class PointHistoryRequest(
    val page: Int = 0,
    val size: Int = 10,
    val memberNo: String? = null
)
