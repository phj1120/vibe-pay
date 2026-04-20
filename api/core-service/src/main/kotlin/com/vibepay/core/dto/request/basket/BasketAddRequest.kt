package com.vibepay.core.dto.request.basket

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class BasketAddRequest(
    @field:NotBlank val goodsNo: String,
    @field:NotBlank val itemNo: String,
    @field:NotNull @field:Min(1) val quantity: Long
)
