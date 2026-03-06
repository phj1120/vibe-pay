package com.api.app.dto.request.payment

data class PaymentConfirmRequest(
    // 공통
    val pgTypeCode: String? = null,
    val authToken: String? = null,
    val orderNo: String? = null,
    val authUrl: String? = null,
    val netCancelUrl: String? = null,

    // 나이스 전용
    val transactionId: String? = null,
    val amount: String? = null,
    val tradeNo: String? = null,
    val mid: String? = null,

    // 이니시스 전용
    val price: Long? = null
)
