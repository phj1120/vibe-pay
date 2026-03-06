package com.api.app.dto.response.order

import com.api.app.dto.response.basket.BasketResponse

data class OrderSheetResponse(
    val items: List<BasketResponse> = emptyList(),
    val ordererName: String? = null,
    val ordererEmail: String? = null,
    val ordererPhone: String? = null,
    val totalProductAmount: Long? = null,
    val totalQuantity: Long? = null
)
