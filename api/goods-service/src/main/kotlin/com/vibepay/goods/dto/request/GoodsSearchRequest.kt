package com.vibepay.goods.dto.request

import jakarta.validation.constraints.Min

data class GoodsSearchRequest(
    val goodsStatusCode: String? = null,
    val goodsName: String? = null,
    @field:Min(0) val page: Int = 0,
    @field:Min(1) val size: Int = 20
)
