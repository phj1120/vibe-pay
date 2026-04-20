package com.vibepay.payment.dto

data class PaymentConfirmRequest(
    val pgTypeCode: String? = null,
    val authToken: String? = null,
    val orderNo: String? = null,
    val authUrl: String? = null,
    val netCancelUrl: String? = null,
    val transactionId: String? = null,
    val niceAmount: String? = null,
    val tradeNo: String? = null,
    val mid: String? = null,
    val price: Long? = null
)
