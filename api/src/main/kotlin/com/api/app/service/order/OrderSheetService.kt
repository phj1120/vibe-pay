package com.api.app.service.order

import com.api.app.dto.response.order.OrderSheetResponse

interface OrderSheetService {
    fun getOrderSheet(email: String, basketNos: List<String>): OrderSheetResponse
}
