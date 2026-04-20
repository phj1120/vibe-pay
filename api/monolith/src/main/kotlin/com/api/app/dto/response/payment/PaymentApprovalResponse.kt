package com.api.app.dto.response.payment

data class PaymentApprovalResponse(
    val approveNo: String? = null,
    val trdNo: String? = null,
    val amount: Long? = null,
    val cardNo: String? = null,
    val cardCode: String? = null
)
