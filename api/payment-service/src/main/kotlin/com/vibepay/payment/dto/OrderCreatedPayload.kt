package com.vibepay.payment.dto

data class OrderCreatedPayload(
    val orderNo: String,
    val memberNo: String,
    val payItems: List<PayItemPayload>,
    val basketNos: List<String> = emptyList()
)

data class PayItemPayload(
    val payWayCode: String,
    val amount: Long,
    val payTypeCode: String = "001",
    // Card payment fields
    val pgTypeCode: String? = null,
    val authToken: String? = null,
    val authUrl: String? = null,
    val netCancelUrl: String? = null,
    val price: Long? = null,
    // Nice payment fields
    val transactionId: String? = null,
    val niceAmount: String? = null,
    val tradeNo: String? = null,
    val mid: String? = null
)
