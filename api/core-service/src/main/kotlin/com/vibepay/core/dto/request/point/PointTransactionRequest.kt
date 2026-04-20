package com.vibepay.core.dto.request.point

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

data class PointTransactionRequest(
    @field:NotNull @field:Positive val amount: Long,
    @field:NotNull val pointTransactionCode: String,
    @field:NotNull val pointTransactionReasonCode: String,
    val pointTransactionReasonNo: String? = null
)
