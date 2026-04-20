package com.vibepay.payment.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class PaymentInitiateRequest(
    @field:NotBlank val orderNumber: String,
    @field:NotNull val amount: Long,
    val productName: String = "상품",
    val buyerName: String = "",
    val buyerTel: String = "",
    val buyerEmail: String = "",
    val paymentMethod: String = "Card"
)
