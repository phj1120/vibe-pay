package com.vibepay.core.dto.request.point

import jakarta.validation.constraints.Min

data class PointHistoryRequest(
    @field:Min(0) val page: Int = 0,
    @field:Min(1) val size: Int = 20
)
