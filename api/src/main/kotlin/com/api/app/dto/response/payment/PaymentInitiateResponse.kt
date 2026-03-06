package com.api.app.dto.response.payment

data class PaymentInitiateResponse(
    val pgType: String? = null,
    val pgTypeCode: String? = null,
    val paymentMethod: String? = null,
    val merchantId: String? = null,
    val merchantKey: String? = null,
    val returnUrl: String? = null,
    val formData: Map<String, String> = emptyMap()
)
