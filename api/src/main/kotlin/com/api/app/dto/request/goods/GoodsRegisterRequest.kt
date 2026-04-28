package com.api.app.dto.request.goods

import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class GoodsRegisterRequest(
    @field:NotBlank(message = "상품명은 필수입니다")
    val goodsName: String,

    @field:NotBlank(message = "상품상태코드는 필수입니다")
    val goodsStatusCode: String,

    @field:NotBlank(message = "상품대표이미지주소는 필수입니다")
    val goodsMainImageUrl: String,

    @field:NotNull(message = "판매가는 필수입니다")
    @field:Min(value = 0, message = "판매가는 0 이상이어야 합니다")
    val salePrice: Long,

    @field:NotNull(message = "공급원가는 필수입니다")
    @field:Min(value = 0, message = "공급원가는 0 이상이어야 합니다")
    val supplyPrice: Long,

    @field:NotEmpty(message = "단품은 최소 1개 이상 등록해야 합니다")
    @field:Valid
    val items: List<GoodsItemRequest>
)
