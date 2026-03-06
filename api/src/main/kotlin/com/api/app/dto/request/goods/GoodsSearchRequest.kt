package com.api.app.dto.request.goods

import jakarta.validation.constraints.Min

data class GoodsSearchRequest(
    val goodsStatusCode: String? = null,
    val goodsName: String? = null,

    @field:Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다")
    val page: Int = 0,

    @field:Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
    val size: Int = 20
)
