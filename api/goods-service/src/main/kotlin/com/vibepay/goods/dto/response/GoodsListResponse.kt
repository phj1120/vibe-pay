package com.vibepay.goods.dto.response

data class GoodsListResponse(
    val goodsNo: String? = null,
    val goodsName: String? = null,
    val goodsStatusCode: String? = null,
    val goodsStatusName: String? = null,
    val goodsMainImageUrl: String? = null,
    val salePrice: Long? = null,
    val supplyPrice: Long? = null
)
