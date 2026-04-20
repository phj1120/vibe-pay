package com.vibepay.core.dto.request.basket

data class BasketModifyRequest(
    val goodsNo: String? = null,
    val itemNo: String? = null,
    val quantity: Long? = null
)
