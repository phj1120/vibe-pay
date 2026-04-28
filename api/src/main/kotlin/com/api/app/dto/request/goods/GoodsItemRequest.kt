package com.api.app.dto.request.goods

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class GoodsItemRequest(
    @field:NotBlank(message = "단품명은 필수입니다")
    val itemName: String,

    @field:NotNull(message = "단품금액은 필수입니다")
    @field:Min(value = 0, message = "단품금액은 0 이상이어야 합니다")
    val itemPrice: Long,

    @field:NotNull(message = "재고수량은 필수입니다")
    @field:Min(value = 0, message = "재고수량은 0 이상이어야 합니다")
    val stock: Long,

    @field:NotBlank(message = "단품상태코드는 필수입니다")
    val goodsStatusCode: String
)
