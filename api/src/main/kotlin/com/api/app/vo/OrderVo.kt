package com.api.app.vo

import java.time.LocalDateTime

data class OrderCompleteGoodsVo(
    val goodsNo: String?,
    val itemNo: String?,
    val goodsName: String?,
    val itemName: String?,
    val salePrice: Long?,
    val quantity: Long?,
    val subtotal: Long?
)

data class OrderCompleteHeaderVo(
    val orderNo: String?,
    val memberNo: String?,
    val orderAcceptDtm: LocalDateTime?,
    val totalAmount: Long?
)

data class OrderListFlatVo(
    val orderNo: String?,
    val orderAcceptDtm: LocalDateTime?,
    val totalAmount: Long?,
    val orderSequence: Long?,
    val orderProcessSequence: Long?,
    val goodsNo: String?,
    val itemNo: String?,
    val goodsName: String?,
    val itemName: String?,
    val salePrice: Long?,
    val quantity: Long?,
    val orderStatusCode: String?,
    val orderStatusName: String?,
    val orderTypeCode: String?,
    val orderTypeName: String?,
    val cancelable: Boolean?,
    val cancelableAmount: Long?
)

data class CancelableOrderItemVo(
    val orderSequence: Long?,
    val orderProcessSequence: Long?,
    val goodsNo: String?,
    val itemNo: String?,
    val goodsName: String?,
    val itemName: String?,
    val salePrice: Long?,
    val quantity: Long?,
    val subtotal: Long?
)

data class RefundDetailVo(
    val payWayCode: String?,
    val payWayName: String?,
    val refundAmount: Long?,
    val pgTypeCode: String?,
    val pgTypeName: String?
)
