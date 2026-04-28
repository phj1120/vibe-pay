package com.api.app.service.payment

import com.api.app.dto.request.payment.PaymentConfirmRequest
import com.api.app.dto.request.payment.PaymentInitiateRequest
import com.api.app.dto.response.payment.PaymentApprovalResponse
import com.api.app.dto.response.payment.PaymentInitiateResponse

interface PaymentService {
    fun initiatePayment(request: PaymentInitiateRequest): PaymentInitiateResponse
    fun approvePayment(request: PaymentConfirmRequest): PaymentApprovalResponse
}
