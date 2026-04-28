package com.api.app.service.payment.method

import com.api.app.dto.request.order.PayRequest
import com.api.app.dto.request.point.PointTransactionRequest
import com.api.app.entity.PayBase
import com.api.app.entity.PayInterfaceLog
import com.api.app.repository.rwdb.pay.PayBaseTrxRepository
import com.api.app.repository.rwdb.pay.PayInterfaceLogTrxRepository
import com.api.app.service.point.PointService
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
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class PointPaymentStrategyTest {

    @InjectMocks
    private lateinit var strategy: PointPaymentStrategy

    @Mock private lateinit var pointService: PointService
    @Mock private lateinit var payBaseTrxRepository: PayBaseTrxRepository
    @Mock private lateinit var payInterfaceLogTrxRepository: PayInterfaceLogTrxRepository
    @Mock private lateinit var objectMapper: ObjectMapper

    @Test
    @DisplayName("포인트 결제 처리 성공")
    fun processPayment_Success() {
        val payRequest = PayRequest(payWayCode = "002", amount = 3000L, payTypeCode = "001")
        var capturedPointRequest: PointTransactionRequest? = null
        val capturingPointService = object : PointService {
            override fun processPointTransaction(memberNo: String, request: PointTransactionRequest) {
                capturedPointRequest = request
            }

            override fun getPointBalance(memberNo: String) = throw UnsupportedOperationException()

            override fun getPointHistoryList(memberNo: String, request: com.api.app.dto.request.point.PointHistoryRequest) =
                throw UnsupportedOperationException()
        }
        val localStrategy = PointPaymentStrategy(capturingPointService, payBaseTrxRepository, payInterfaceLogTrxRepository, objectMapper)

        given(payBaseTrxRepository.save(any(PayBase::class.java))).willAnswer {
            (it.arguments[0] as PayBase).apply { payNo = "P002" }
        }
        doReturn("""{"request":true}""").`when`(objectMapper).writeValueAsString(any(PointTransactionRequest::class.java))

        val result = localStrategy.processPayment("M001", "O001", payRequest)

        assertThat(capturedPointRequest).isNotNull
        assertThat(capturedPointRequest!!.amount).isEqualTo(3000L)
        assertThat(capturedPointRequest!!.pointTransactionReasonNo).isEqualTo("P002")
        assertThat(result.payFinishDateTime).isNotNull()

        val logCaptor = ArgumentCaptor.forClass(PayInterfaceLog::class.java)
        verify(payInterfaceLogTrxRepository, times(2)).save(logCaptor.capture())
        assertThat(logCaptor.allValues.map { it.payLogCode }).containsExactly("001", "002")
    }

    @Test
    @DisplayName("결제 방식 코드는 포인트")
    fun getPayWayCode_Success() {
        assertThat(strategy.getPayWayCode()).isEqualTo("002")
    }
}
