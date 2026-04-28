package com.api.app.dto.request.order

import com.api.app.dto.request.payment.PaymentConfirmRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class PayRequest(
    @field:NotBlank(message = "결제방식코드는 필수입니다")
    val payWayCode: String,

    @field:NotNull(message = "결제금액은 필수입니다")
    val amount: Long,

    @field:NotBlank(message = "결제유형코드는 필수입니다")
    val payTypeCode: String,

    @field:Valid
    val paymentConfirmRequest: PaymentConfirmRequest? = null
)
