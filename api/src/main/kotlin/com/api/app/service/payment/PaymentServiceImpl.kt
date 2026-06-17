package com.api.app.service.payment

import com.api.app.dto.request.payment.PaymentConfirmRequest
import com.api.app.dto.request.payment.PaymentInitiateRequest
import com.api.app.dto.response.payment.PaymentApprovalResponse
import com.api.app.dto.response.payment.PaymentInitiateResponse
import com.api.app.emum.PAY005
import com.api.app.service.payment.strategy.PaymentGatewayFactory
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PaymentServiceImpl(
    private val paymentGatewayFactory: PaymentGatewayFactory
) : PaymentService {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun initiatePayment(request: PaymentInitiateRequest): PaymentInitiateResponse {
        log.info("Payment initiate started. orderNumber={}, amount={}", request.orderNumber, request.amount)

        val strategy = request.pgType
            ?.let { pgType ->
                PAY005.entries.firstOrNull { it.name.equals(pgType, ignoreCase = true) || it.code == pgType }
                    ?: throw IllegalArgumentException("지원하지 않는 PG입니다: $pgType")
            }
            ?.let { paymentGatewayFactory.getStrategy(it) }
            ?: paymentGatewayFactory.selectByWeight()
        val response = strategy.initiatePayment(request)

        log.info("Payment initiate completed. orderNumber={}, pgType={}", request.orderNumber, response.pgType)
        return response
    }

    override fun approvePayment(request: PaymentConfirmRequest): PaymentApprovalResponse {
        log.info("Payment approval started. orderNo={}, pgTypeCode={}", request.orderNo, request.pgTypeCode)

        val pgType = PAY005.findByCode(request.pgTypeCode!!)
            ?: throw IllegalArgumentException("지원하지 않는 PG 코드입니다: ${request.pgTypeCode}")
        val strategy = paymentGatewayFactory.getStrategy(pgType)
        val response = strategy.approvePayment(request)

        log.info("Payment approval completed. orderNo={}, approveNo={}", request.orderNo, response.approveNo)
        return response
    }
}
