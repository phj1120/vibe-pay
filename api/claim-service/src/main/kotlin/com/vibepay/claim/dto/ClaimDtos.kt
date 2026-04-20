package com.vibepay.claim.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class CancelRequest(
    @field:NotBlank val memberNo: String,
    @field:NotEmpty val targets: List<ClaimTargetRequest>,
    val claimReason: String? = null
)

data class ClaimTargetRequest(
    @field:NotBlank val orderNo: String,
    @field:NotNull val orderSequence: Long,
    @field:NotNull val orderProcessSequence: Long
)

data class OrderCancelInfoResponse(
    val orderNo: String,
    val memberNo: String,
    val cancelableItems: List<CancelableItem>,
    val payments: List<PaymentInfo>
)

data class CancelableItem(
    val orderSequence: Long,
    val orderProcessSequence: Long,
    val goodsNo: String,
    val itemNo: String,
    val salePrice: Long,
    val quantity: Long
)

data class PaymentInfo(
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
