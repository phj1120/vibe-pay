package com.vibepay.goods.dto.request

import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class GoodsModifyRequest(
    @field:NotBlank val goodsName: String,
    @field:NotBlank val goodsStatusCode: String,
    @field:NotBlank val goodsMainImageUrl: String,
    @field:NotNull @field:Min(0) val salePrice: Long,
    @field:NotNull @field:Min(0) val supplyPrice: Long,
    @field:NotEmpty @field:Valid val items: List<GoodsItemRequest>
)
