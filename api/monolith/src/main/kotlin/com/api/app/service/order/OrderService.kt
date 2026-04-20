package com.api.app.service.order

import com.api.app.dto.request.order.OrderRequest
import com.api.app.dto.response.order.CancelableOrderResponse
import com.api.app.dto.response.order.OrderCompleteResponse
import com.api.app.dto.response.order.OrderListResponse

interface OrderService {
    fun generateOrderNumber(): String
    fun createOrder(request: OrderRequest)
    fun getOrderComplete(orderNo: String, memberNo: String): OrderCompleteResponse
    fun getOrderList(memberNo: String): List<OrderListResponse>
    fun getCancelableOrders(orderNo: String, memberNo: String): CancelableOrderResponse
}
