package com.vibepay.goods.dto.response

data class GoodsItemResponse(
    val goodsNo: String? = null,
    val itemNo: String? = null,
    val itemName: String? = null,
    val itemPrice: Long? = null,
    val stock: Long? = null,
    val goodsStatusCode: String? = null,
    val goodsStatusName: String? = null,
    val isSoldOut: Boolean? = null
)
