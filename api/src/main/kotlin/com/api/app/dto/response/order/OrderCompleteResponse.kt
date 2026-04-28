package com.api.app.dto.response.order

import java.time.LocalDateTime

data class OrderCompleteResponse(
    val orderNo: String? = null,
    val memberNo: String? = null,
    val orderAcceptDtm: LocalDateTime? = null,
    val totalAmount: Long? = null,
    val goodsList: List<OrderCompleteGoods> = emptyList(),
    val paymentList: List<OrderCompletePayment> = emptyList()
) {
    data class OrderCompleteGoods(
        val goodsNo: String? = null,
        val itemNo: String? = null,
        val goodsName: String? = null,
        val itemName: String? = null,
        val salePrice: Long? = null,
        val quantity: Long? = null,
        val subtotal: Long? = null
    )

    data class OrderCompletePayment(
        val payWayCode: String? = null,
        val payWayName: String? = null,
        val amount: Long? = null,
        val pgTypeCode: String? = null,
        val pgTypeName: String? = null
    )
}
