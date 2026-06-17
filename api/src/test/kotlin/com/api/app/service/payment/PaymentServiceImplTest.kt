package com.api.app.service.payment

import com.api.app.dto.request.payment.PaymentConfirmRequest
import com.api.app.dto.request.payment.PaymentInitiateRequest
import com.api.app.dto.response.payment.PaymentApprovalResponse
import com.api.app.dto.response.payment.PaymentInitiateResponse
import com.api.app.emum.PAY005
import com.api.app.service.payment.strategy.PaymentGatewayFactory
import com.api.app.service.payment.strategy.PaymentGatewayStrategy
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class PaymentServiceImplTest {

    @InjectMocks
    private lateinit var paymentService: PaymentServiceImpl

    @Mock
    private lateinit var paymentGatewayFactory: PaymentGatewayFactory

    @Mock
    private lateinit var paymentGatewayStrategy: PaymentGatewayStrategy

    @Test
    @DisplayName("initiatePayment delegates to selected gateway")
    fun initiatePaymentDelegatesToSelectedGateway() {
        val request = PaymentInitiateRequest(
            orderNumber = "20260428O000001",
            amount = 12000L,
            productName = "Speaker",
            buyerName = "Kim",
            buyerEmail = "kim@test.com",
            buyerTel = "01012341234"
        )
        val response = PaymentInitiateResponse(pgType = "INICIS", pgTypeCode = PAY005.INICIS.code)
        given(paymentGatewayFactory.selectByWeight()).willReturn(paymentGatewayStrategy)
        given(paymentGatewayStrategy.initiatePayment(request)).willReturn(response)

        val result = paymentService.initiatePayment(request)

        assertThat(result).isEqualTo(response)
        verify(paymentGatewayFactory).selectByWeight()
        verify(paymentGatewayStrategy).initiatePayment(request)
    }

    @Test
    @DisplayName("initiatePayment delegates to requested test PG")
    fun initiatePaymentDelegatesToRequestedTestPg() {
        val request = PaymentInitiateRequest(
            orderNumber = "20260428O000001",
            pgType = "TEST",
            amount = 12000L,
            productName = "Speaker",
            buyerName = "Kim",
            buyerEmail = "kim@test.com",
            buyerTel = "01012341234"
        )
        val response = PaymentInitiateResponse(pgType = "TEST", pgTypeCode = PAY005.TEST.code)
        given(paymentGatewayFactory.getStrategy(PAY005.TEST)).willReturn(paymentGatewayStrategy)
        given(paymentGatewayStrategy.initiatePayment(request)).willReturn(response)

        val result = paymentService.initiatePayment(request)

        assertThat(result).isEqualTo(response)
        verify(paymentGatewayFactory).getStrategy(PAY005.TEST)
        verify(paymentGatewayStrategy).initiatePayment(request)
    }

    @Test
    @DisplayName("approvePayment delegates by PG code")
    fun approvePaymentDelegatesByPgCode() {
        val request = PaymentConfirmRequest(pgTypeCode = PAY005.NICE.code, orderNo = "20260428O000001")
        val response = PaymentApprovalResponse(approveNo = "APPROVE-1", trdNo = "TRD-1")
        given(paymentGatewayFactory.getStrategy(PAY005.NICE)).willReturn(paymentGatewayStrategy)
        given(paymentGatewayStrategy.approvePayment(request)).willReturn(response)

        val result = paymentService.approvePayment(request)

        assertThat(result).isEqualTo(response)
        verify(paymentGatewayFactory).getStrategy(PAY005.NICE)
        verify(paymentGatewayStrategy).approvePayment(request)
    }

    @Test
    @DisplayName("approvePayment throws for unsupported PG code")
    fun approvePaymentThrowsForUnsupportedPgCode() {
        val request = PaymentConfirmRequest(pgTypeCode = "998", orderNo = "20260428O000001")

        assertThatThrownBy { paymentService.approvePayment(request) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("지원하지 않는 PG 코드입니다: 998")
    }
}
