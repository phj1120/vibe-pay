package com.api.app.dto.request.claim

data class CancelRequest(
    val memberNo: String = "",
    val targets: List<ClaimTargetRequest> = emptyList()
)
