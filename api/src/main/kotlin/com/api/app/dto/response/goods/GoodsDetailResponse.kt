package com.api.app.dto.response.goods

import java.time.LocalDateTime

data class GoodsDetailResponse(
    val goodsNo: String? = null,
    val goodsName: String? = null,
    val goodsStatusCode: String? = null,
    val goodsStatusName: String? = null,
    val goodsMainImageUrl: String? = null,
    val salePrice: Long? = null,
    val supplyPrice: Long? = null,
    val items: List<GoodsItemResponse> = emptyList(),
    val registDateTime: LocalDateTime? = null,
    val modifyDateTime: LocalDateTime? = null
)
