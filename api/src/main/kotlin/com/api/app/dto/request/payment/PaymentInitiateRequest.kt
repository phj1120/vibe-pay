package com.api.app.dto.request.payment

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class PaymentInitiateRequest(
    @field:NotBlank(message = "주문번호는 필수입니다")
    val orderNumber: String,

    val paymentMethod: String? = null,
    val pgType: String? = null,

    @field:NotNull(message = "결제금액은 필수입니다")
    val amount: Long,

    @field:NotBlank(message = "상품명은 필수입니다")
    val productName: String,

    @field:NotBlank(message = "구매자명은 필수입니다")
    val buyerName: String,

    @field:NotBlank(message = "이메일은 필수입니다")
    val buyerEmail: String,

    @field:NotBlank(message = "전화번호는 필수입니다")
    val buyerTel: String
)
