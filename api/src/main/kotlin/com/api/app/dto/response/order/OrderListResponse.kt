package com.api.app.dto.response.order

import java.time.LocalDateTime

data class OrderListResponse(
    val orderNo: String? = null,
    val orderAcceptDtm: LocalDateTime? = null,
    val totalAmount: Long? = null,
    val goodsList: List<OrderListGoods> = emptyList()
) {
    data class OrderListGoods(
        val orderSequence: Long? = null,
        val orderProcessSequence: Long? = null,
        val goodsNo: String? = null,
        val itemNo: String? = null,
        val goodsName: String? = null,
        val itemName: String? = null,
        val salePrice: Long? = null,
        val quantity: Long? = null,
        val orderStatusCode: String? = null,
        val orderStatusName: String? = null,
        val orderTypeCode: String? = null,
        val orderTypeName: String? = null,
        val cancelable: Boolean? = null,
        val cancelableAmount: Long? = null
    )
}
