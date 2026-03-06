package com.api.app.service.payment.method

import com.api.app.dto.request.order.PayRequest
import com.api.app.entity.PayBase

interface PaymentMethodStrategy {
    fun processPayment(memberNo: String, orderNo: String, payRequest: PayRequest): PayBase
    fun getPayWayCode(): String
}
