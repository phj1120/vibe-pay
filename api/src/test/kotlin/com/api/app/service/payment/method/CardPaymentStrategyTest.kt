package com.api.app.service.payment.method

import com.api.app.dto.request.order.PayRequest
import com.api.app.dto.request.payment.PaymentConfirmRequest
import com.api.app.dto.response.payment.PaymentApprovalResponse
import com.api.app.emum.PAY005
import com.api.app.entity.PayBase
import com.api.app.entity.PayInterfaceLog
import com.api.app.repository.rwdb.pay.PayBaseTrxRepository
import com.api.app.repository.rwdb.pay.PayInterfaceLogTrxRepository
import com.api.app.service.payment.strategy.PaymentGatewayFactory
import com.api.app.service.payment.strategy.PaymentGatewayStrategy
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class CardPaymentStrategyTest {

    @InjectMocks
    private lateinit var strategy: CardPaymentStrategy

    @Mock private lateinit var paymentGatewayFactory: PaymentGatewayFactory
    @Mock private lateinit var payBaseTrxRepository: PayBaseTrxRepository
    @Mock private lateinit var payInterfaceLogTrxRepository: PayInterfaceLogTrxRepository
    @Mock private lateinit var objectMapper: ObjectMapper
    @Mock private lateinit var paymentGatewayStrategy: PaymentGatewayStrategy

    @Test
    @DisplayName("카드 결제 처리 성공")
    fun processPayment_Success() {
        val confirmRequest = PaymentConfirmRequest(pgTypeCode = PAY005.NICE.code, orderNo = "O001")
        val payRequest = PayRequest(payWayCode = "001", amount = 10000L, payTypeCode = "001", paymentConfirmRequest = confirmRequest)

        given(paymentGatewayFactory.getStrategy(PAY005.NICE)).willReturn(paymentGatewayStrategy)
        given(payBaseTrxRepository.save(any(PayBase::class.java))).willAnswer {
            (it.arguments[0] as PayBase).apply { payNo = "P001" }
        }
        given(objectMapper.writeValueAsString(confirmRequest)).willReturn("""{"request":true}""")
        given(paymentGatewayStrategy.approvePayment(confirmRequest))
            .willReturn(PaymentApprovalResponse(approveNo = "APP-1", trdNo = "TRD-1"))
        given(objectMapper.writeValueAsString(any(PaymentApprovalResponse::class.java))).willReturn("""{"approve":true}""")

        val result = strategy.processPayment("M001", "O001", payRequest)

        assertThat(result.payNo).isEqualTo("P001")
        assertThat(result.approveNo).isEqualTo("APP-1")
        assertThat(result.trdNo).isEqualTo("TRD-1")
        assertThat(result.payFinishDateTime).isNotNull()

        val logCaptor = ArgumentCaptor.forClass(PayInterfaceLog::class.java)
        verify(payInterfaceLogTrxRepository, times(2)).save(logCaptor.capture())
        assertThat(logCaptor.allValues.map { it.payLogCode }).containsExactly("001", "002")
    }

    @Test
    @DisplayName("결제 방식 코드는 신용카드")
    fun getPayWayCode_Success() {
        assertThat(strategy.getPayWayCode()).isEqualTo("001")
    }
}
