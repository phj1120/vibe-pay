package com.vibepay.goods.dto.internal

data class GoodsValidateResponse(
    val valid: Boolean,
    val items: List<GoodsItemSnapshot> = emptyList(),
    val errorMessage: String? = null
)

data class GoodsItemSnapshot(
    val goodsNo: String,
    val itemNo: String,
    val goodsName: String,
    val itemName: String,
    val salePrice: Long,
    val supplyPrice: Long,
    val stock: Long
)
