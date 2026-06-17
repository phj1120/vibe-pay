package com.api.app.service.payment.strategy

import com.api.app.dto.request.payment.PaymentCancelRequest
import com.api.app.dto.request.payment.PaymentConfirmRequest
import com.api.app.dto.request.payment.PaymentInitiateRequest
import com.api.app.emum.PAY005
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class TestPaymentStrategyTest {

    private val strategy = TestPaymentStrategy()

    @Test
    @DisplayName("테스트 PG 결제 초기화 응답을 생성한다")
    fun initiatePaymentReturnsTestPgResponse() {
        val request = PaymentInitiateRequest(
            orderNumber = "20260428O000001",
            paymentMethod = "CARD",
            amount = 12000L,
            productName = "Speaker",
            buyerName = "Kim",
            buyerEmail = "kim@test.com",
            buyerTel = "01012341234"
        )

        val response = strategy.initiatePayment(request)

        assertThat(response.pgType).isEqualTo("TEST")
        assertThat(response.pgTypeCode).isEqualTo(PAY005.TEST.code)
        assertThat(response.formData["orderNo"]).isEqualTo(request.orderNumber)
        assertThat(response.formData["amount"]).isEqualTo("12000")
    }

    @Test
    @DisplayName("테스트 PG 승인은 외부 호출 없이 승인 응답을 생성한다")
    fun approvePaymentReturnsSyntheticApproval() {
        val request = PaymentConfirmRequest(
            pgTypeCode = PAY005.TEST.code,
            orderNo = "20260428O000001",
            amount = "12000"
        )

        val response = strategy.approvePayment(request)

        assertThat(response.approveNo).startsWith("TESTAPPROVE-20260428O000001-")
        assertThat(response.trdNo).startsWith("TESTTRD-20260428O000001-")
        assertThat(response.amount).isEqualTo(12000L)
        assertThat(response.cardCode).isEqualTo("TEST")
    }

    @Test
    @DisplayName("테스트 PG 취소는 외부 호출 없이 완료된다")
    fun cancelPaymentByOrderCompletes() {
        strategy.cancelPaymentByOrder(
            PaymentCancelRequest(
                pgTypeCode = PAY005.TEST.code,
                transactionId = "TESTTRD-20260428O000001-20260428101010000",
                orderNo = "20260428O000001",
                cancelAmount = 12000L,
                cancelReason = "load test cancel",
                partialCancelCode = "0",
                originalAmount = 12000L,
                cancelableAmount = 12000L
            )
        )
    }
}
