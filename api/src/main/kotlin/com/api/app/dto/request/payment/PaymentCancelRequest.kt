package com.api.app.dto.request.payment

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

data class PaymentCancelRequest(
    @field:NotNull(message = "PG사 코드는 필수입니다")
    val pgTypeCode: String,

    @field:NotNull(message = "거래번호는 필수입니다")
    val transactionId: String,

    @field:NotNull(message = "주문번호는 필수입니다")
    val orderNo: String,

    @field:NotNull(message = "취소금액은 필수입니다")
    @field:Positive(message = "취소금액은 0보다 커야 합니다")
    val cancelAmount: Long,

    @field:NotNull(message = "취소사유는 필수입니다")
    val cancelReason: String,

    val partialCancelCode: String? = null,
    val originalAmount: Long? = null,
    val cancelableAmount: Long? = null
)
