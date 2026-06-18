package com.api.app.vo

data class OrderCompletePaymentVo(
    val payWayCode: String?,
    val amount: Long?,
    val pgTypeCode: String?
)
