package com.api.app.service.payment.strategy

import com.api.app.dto.request.payment.PaymentCancelRequest
import com.api.app.dto.request.payment.PaymentConfirmRequest
import com.api.app.dto.request.payment.PaymentInitiateRequest
import com.api.app.dto.response.payment.PaymentApprovalResponse
import com.api.app.dto.response.payment.PaymentInitiateResponse
import com.api.app.emum.PAY005

interface PaymentGatewayStrategy {
    fun initiatePayment(request: PaymentInitiateRequest): PaymentInitiateResponse
    fun approvePayment(request: PaymentConfirmRequest): PaymentApprovalResponse
    fun cancelPayment(request: PaymentConfirmRequest)
    fun cancelPaymentByOrder(request: PaymentCancelRequest)
    fun getPgType(): PAY005
}
