package com.vibepay.payment.dto

data class CancelByOrderRequest(
    val claimNo: String,
    val memberNo: String,
    val cancelAmounts: Map<String, Long>
)
