package com.vibepay.core.dto.response.basket

import java.time.LocalDateTime

data class BasketResponse(
    val basketNo: String? = null,
    val memberNo: String? = null,
    val goodsNo: String? = null,
    val goodsName: String? = null,
    val goodsStatusCode: String? = null,
    val goodsMainImageUrl: String? = null,
    val salePrice: Long? = null,
    val itemNo: String? = null,
    val itemName: String? = null,
    val itemPrice: Long? = null,
    val itemStatusCode: String? = null,
    val stock: Long? = null,
    val quantity: Long? = null,
    val isOrder: Boolean? = null,
    val registDateTime: LocalDateTime? = null
)
