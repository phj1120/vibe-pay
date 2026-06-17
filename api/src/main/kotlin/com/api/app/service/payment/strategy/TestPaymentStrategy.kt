package com.api.app.service.payment.strategy

import com.api.app.dto.request.payment.PaymentCancelRequest
import com.api.app.dto.request.payment.PaymentConfirmRequest
import com.api.app.dto.request.payment.PaymentInitiateRequest
import com.api.app.dto.response.payment.PaymentApprovalResponse
import com.api.app.dto.response.payment.PaymentInitiateResponse
import com.api.app.emum.PAY005
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class TestPaymentStrategy : PaymentGatewayStrategy {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun initiatePayment(request: PaymentInitiateRequest): PaymentInitiateResponse {
        log.info("Test PG payment initiate completed. orderNumber={}", request.orderNumber)

        return PaymentInitiateResponse(
            pgType = "TEST",
            pgTypeCode = PAY005.TEST.code,
            paymentMethod = request.paymentMethod,
            merchantId = "test-pg",
            merchantKey = "test-pg-key",
            returnUrl = "",
            formData = mapOf(
                "pgTypeCode" to PAY005.TEST.code,
                "orderNo" to request.orderNumber,
                "amount" to request.amount.toString(),
                "authToken" to buildToken("TESTAUTH", request.orderNumber)
            )
        )
    }

    override fun approvePayment(request: PaymentConfirmRequest): PaymentApprovalResponse {
        val orderNo = request.orderNo ?: "UNKNOWN"
        val amount = request.price ?: request.amount?.toLongOrNull()

        log.info("Test PG payment approved. orderNo={}, amount={}", orderNo, amount)

        return PaymentApprovalResponse(
            approveNo = buildToken("TESTAPPROVE", orderNo),
            trdNo = buildToken("TESTTRD", orderNo),
            amount = amount,
            cardNo = "411111******1111",
            cardCode = "TEST"
        )
    }

    override fun cancelPayment(request: PaymentConfirmRequest) {
        log.info("Test PG payment net cancel completed. orderNo={}", request.orderNo)
    }

    override fun cancelPaymentByOrder(request: PaymentCancelRequest) {
        log.info(
            "Test PG order cancel completed. orderNo={}, transactionId={}, cancelAmount={}",
            request.orderNo,
            request.transactionId,
            request.cancelAmount
        )
    }

    override fun getPgType(): PAY005 = PAY005.TEST

    private fun buildToken(prefix: String, orderNo: String): String {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
        return "$prefix-$orderNo-$timestamp"
    }
}
