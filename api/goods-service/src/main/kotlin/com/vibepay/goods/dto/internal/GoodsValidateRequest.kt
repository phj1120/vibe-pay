package com.vibepay.goods.dto.internal

data class GoodsValidateRequest(
    val items: List<GoodsValidateItem>
)

data class GoodsValidateItem(
    val goodsNo: String,
    val itemNo: String,
    val quantity: Long,
    val expectedSalePrice: Long
)
