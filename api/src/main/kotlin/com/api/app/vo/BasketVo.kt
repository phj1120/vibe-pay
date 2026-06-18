package com.api.app.vo

import java.time.LocalDateTime

data class BasketVo(
    val basketNo: String?,
    val memberNo: String?,
    val goodsNo: String?,
    val goodsName: String?,
    val goodsStatusCode: String?,
    val goodsMainImageUrl: String?,
    val salePrice: Long?,
    val itemNo: String?,
    val itemName: String?,
    val itemPrice: Long?,
    val itemStatusCode: String?,
    val stock: Long?,
    val quantity: Long?,
    val isOrder: Boolean?,
    val registDateTime: LocalDateTime?
)
