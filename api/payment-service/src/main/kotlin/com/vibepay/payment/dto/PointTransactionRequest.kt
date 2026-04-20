package com.vibepay.payment.dto

data class PointTransactionRequest(
    val amount: Long,
    val pointTransactionCode: String,
    val pointTransactionReasonCode: String,
    val pointTransactionReasonNo: String? = null
)
