package com.api.app.vo

import java.time.LocalDateTime

data class GoodsListVo(
    val goodsNo: String?,
    val goodsName: String?,
    val goodsStatusCode: String?,
    val goodsStatusName: String?,
    val goodsMainImageUrl: String?,
    val salePrice: Long?,
    val supplyPrice: Long?
)

data class GoodsDetailVo(
    val goodsNo: String?,
    val goodsName: String?,
    val goodsStatusCode: String?,
    val goodsStatusName: String?,
    val goodsMainImageUrl: String?,
    val salePrice: Long?,
    val supplyPrice: Long?,
    val registDateTime: LocalDateTime?,
    val modifyDateTime: LocalDateTime?
)
