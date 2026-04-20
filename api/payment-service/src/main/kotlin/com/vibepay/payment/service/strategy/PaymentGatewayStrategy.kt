package com.vibepay.payment.service.strategy

import com.api.app.emum.PAY005
import com.vibepay.payment.dto.PaymentConfirmRequest
import com.vibepay.payment.dto.PaymentInitiateRequest
import com.vibepay.payment.dto.PaymentInitiateResponse

interface PaymentGatewayStrategy {
    fun getPgType(): PAY005
    fun initiatePayment(request: PaymentInitiateRequest): PaymentInitiateResponse
    fun approvePayment(request: PaymentConfirmRequest): PaymentApprovalResult
    fun cancelPayment(payNo: String, orderNo: String, transactionId: String, cancelAmount: Long, cancelableAmount: Long?, originalAmount: Long?, cancelReason: String)
}

data class PaymentApprovalResult(
    val approveNo: String? = null,
    val trdNo: String? = null,
    val amount: Long? = null,
    val cardNo: String? = null,
    val cardCode: String? = null
)
