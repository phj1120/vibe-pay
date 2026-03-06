package com.api.app.dto.request.basket

import jakarta.validation.constraints.Min

data class BasketModifyRequest(
    val goodsNo: String? = null,
    val itemNo: String? = null,

    @field:Min(value = 1, message = "수량은 1 이상이어야 합니다")
    val quantity: Long? = null
)
