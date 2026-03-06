package com.api.app.dto.request.claim

data class ClaimTargetRequest(
    val orderNo: String = "",
    val orderSequence: Long = 0,
    val orderProcessSequence: Long = 0
)
