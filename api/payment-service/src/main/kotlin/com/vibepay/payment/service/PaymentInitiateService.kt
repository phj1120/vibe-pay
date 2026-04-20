package com.vibepay.payment.service

import com.vibepay.payment.dto.PaymentInitiateRequest
import com.vibepay.payment.dto.PaymentInitiateResponse
import com.vibepay.payment.service.strategy.PaymentGatewayFactory
import org.springframework.stereotype.Service

@Service
class PaymentInitiateService(private val factory: PaymentGatewayFactory) {

    fun initiate(request: PaymentInitiateRequest): PaymentInitiateResponse =
        factory.selectByWeight().initiatePayment(request)
}
