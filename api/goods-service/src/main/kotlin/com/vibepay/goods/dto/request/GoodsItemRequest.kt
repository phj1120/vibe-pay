package com.vibepay.goods.dto.request

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class GoodsItemRequest(
    @field:NotBlank val itemName: String,
    @field:NotNull @field:Min(0) val itemPrice: Long,
    @field:NotNull @field:Min(0) val stock: Long,
    @field:NotBlank val goodsStatusCode: String
)
