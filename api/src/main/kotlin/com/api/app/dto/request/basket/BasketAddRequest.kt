package com.api.app.dto.request.basket

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class BasketAddRequest(
    @field:NotBlank(message = "상품번호는 필수입니다")
    val goodsNo: String,

    @field:NotBlank(message = "단품번호는 필수입니다")
    val itemNo: String,

    @field:NotNull(message = "수량은 필수입니다")
    @field:Min(value = 1, message = "수량은 1 이상이어야 합니다")
    val quantity: Long
)
