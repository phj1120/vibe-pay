package com.api.app.dto.internal

data class OrderCancelInfoResponse(
    val orderNo: String,
    val memberNo: String,
    val cancelableItems: List<CancelableItemDto>,
    val payments: List<PaymentInfoDto>
)

data class CancelableItemDto(
    val orderSequence: Long,
    val orderProcessSequence: Long,
    val goodsNo: String,
    val itemNo: String,
    val salePrice: Long,
    val quantity: Long
)

data class PaymentInfoDto(
    val payNo: String,
    val payWayCode: String,
    val cancelableAmount: Long,
    val pgTypeCode: String? = null,
    val trdNo: String? = null,
    val originalAmount: Long? = null
)

data class CreateCancelDetailsRequest(
    val claimNo: String,
    val memberNo: String,
    val targets: List<CancelTargetDto>
)

data class CancelTargetDto(
    val orderNo: String,
    val orderSequence: Long,
    val orderProcessSequence: Long
)
