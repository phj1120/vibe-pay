package com.api.app.dto.request.payment

data class InicisApprovalRequest(
    val mid: String? = null,
    val authToken: String? = null,
    val timestamp: String? = null,
    val signature: String? = null,
    val verification: String? = null,
    val charset: String? = null,
    val format: String? = null,
    val price: Long? = null
)
