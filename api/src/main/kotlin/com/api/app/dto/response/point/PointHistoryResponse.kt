package com.api.app.dto.response.point

import java.time.LocalDateTime

data class PointHistoryResponse(
    val pointHistoryNo: String? = null,
    val amount: Long? = null,
    val pointTransactionCode: String? = null,
    val pointTransactionReasonCode: String? = null,
    val pointTransactionReasonNo: String? = null,
    val startDateTime: LocalDateTime? = null,
    val endDateTime: LocalDateTime? = null,
    val remainPoint: Long? = null,
    val createdDate: LocalDateTime? = null
)
