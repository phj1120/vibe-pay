package com.vibepay.payment.dto

data class PaymentInitiateResponse(
    val pgType: String,
    val pgTypeCode: String,
    val paymentMethod: String,
    val merchantId: String,
    val merchantKey: String,
    val returnUrl: String,
    val formData: Map<String, Any?>
)
