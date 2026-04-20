package com.api.app.dto.request.point

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive

data class PointTransactionRequest(
    @field:NotNull(message = "금액은 필수입니다")
    @field:Positive(message = "금액은 0보다 커야 합니다")
    val amount: Long,

    @field:NotNull(message = "포인트적립사용코드는 필수입니다")
    @field:Pattern(regexp = "^(001|002)$", message = "포인트적립사용코드는 001(적립) 또는 002(사용)만 가능합니다")
    val pointTransactionCode: String,

    @field:NotNull(message = "포인트적립사용사유코드는 필수입니다")
    @field:Pattern(regexp = "^(001|002)$", message = "포인트적립사용사유코드는 001(기타) 또는 002(주문)만 가능합니다")
    val pointTransactionReasonCode: String,

    val pointTransactionReasonNo: String? = null
)
