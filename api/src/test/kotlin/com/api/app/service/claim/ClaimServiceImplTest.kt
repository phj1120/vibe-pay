package com.api.app.service.claim

import com.api.app.dto.request.claim.CancelRequest
import com.api.app.dto.request.claim.ClaimTargetRequest
import com.api.app.dto.request.payment.PaymentCancelRequest
import com.api.app.dto.response.payment.PaymentApprovalResponse
import com.api.app.dto.response.payment.PaymentInitiateResponse
import com.api.app.emum.ORD001
import com.api.app.emum.ORD002
import com.api.app.emum.PAY001
import com.api.app.emum.PAY002
import com.api.app.emum.PAY005
import com.api.app.entity.OrderDetail
import com.api.app.entity.OrderDetailId
import com.api.app.entity.OrderGoods
import com.api.app.entity.OrderGoodsId
import com.api.app.entity.PayBase
import com.api.app.entity.PayInterfaceLog
import com.api.app.repository.rodb.order.OrderDetailRepository
import com.api.app.repository.rodb.order.OrderGoodsRepository
import com.api.app.repository.rodb.pay.PayBaseRepository
import com.api.app.repository.rwdb.order.OrderBaseTrxRepository
import com.api.app.repository.rwdb.order.OrderDetailTrxRepository
import com.api.app.repository.rwdb.pay.PayBaseTrxRepository
import com.api.app.repository.rwdb.pay.PayInterfaceLogTrxRepository
import com.api.app.service.payment.strategy.PaymentGatewayFactory
import com.api.app.service.payment.strategy.PaymentGatewayStrategy
import com.api.app.service.point.PointService
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class ClaimServiceImplTest {

    @InjectMocks
    private lateinit var claimService: ClaimServiceImpl

    @Mock private lateinit var orderBaseTrxRepository: OrderBaseTrxRepository
    @Mock private lateinit var orderDetailRepository: OrderDetailRepository
    @Mock private lateinit var orderDetailTrxRepository: OrderDetailTrxRepository
    @Mock private lateinit var orderGoodsRepository: OrderGoodsRepository
    @Mock private lateinit var payBaseRepository: PayBaseRepository
    @Mock private lateinit var payBaseTrxRepository: PayBaseTrxRepository
    @Mock private lateinit var payInterfaceLogTrxRepository: PayInterfaceLogTrxRepository
    @Mock private lateinit var pointService: PointService
    @Mock private lateinit var paymentGatewayFactory: PaymentGatewayFactory
    @Mock private lateinit var objectMapper: ObjectMapper
    @Mock private lateinit var paymentGatewayStrategy: PaymentGatewayStrategy

    private fun cancelRequest() = CancelRequest(
        memberNo = "M001",
        targets = listOf(ClaimTargetRequest(orderNo = "O001", orderSequence = 1L, orderProcessSequence = 1L))
    )

    private fun orderDetail(
        orderTypeCode: String = ORD001.ORDER.code,
        orderStatusCode: String = ORD002.ORDER_RECEIVED.code
    ) = OrderDetail().apply {
        id = OrderDetailId(orderNo = "O001", orderSequence = 1L, orderProcessSequence = 1L)
        goodsNo = "G001"
        itemNo = "I001"
        quantity = 2L
        this.orderTypeCode = orderTypeCode
        this.orderStatusCode = orderStatusCode
    }

    private fun orderGoods() = OrderGoods().apply {
        id = OrderGoodsId(orderNo = "O001", goodsNo = "G001", itemNo = "I001")
        salePrice = 10000L
        supplyPrice = 7000L
        goodsName = "테스트상품"
        itemName = "기본"
    }

    private fun cardPayment(cancelableAmount: Long = 20000L) = PayBase().apply {
        payNo = "P001"
        payTypeCode = PAY001.PAYMENT.code
        payWayCode = PAY002.CREDIT_CARD.code
        orderNo = "O001"
        memberNo = "M001"
        amount = 20000L
        this.cancelableAmount = cancelableAmount
        pgTypeCode = PAY005.NICE.code
        trdNo = "TRD-1"
    }

    @Test
    @DisplayName("주문 취소 성공")
    fun cancelOrder_Success() {
        val request = cancelRequest()
        val originalDetail = orderDetail()
        val originalPayment = cardPayment()
        var capturedCancelRequest: PaymentCancelRequest? = null
        var savedCancelDetail: OrderDetail? = null
        var savedInterfaceLog: PayInterfaceLog? = null
        val capturingGatewayStrategy = object : PaymentGatewayStrategy {
            override fun initiatePayment(request: com.api.app.dto.request.payment.PaymentInitiateRequest): PaymentInitiateResponse {
                throw UnsupportedOperationException()
            }

            override fun approvePayment(request: com.api.app.dto.request.payment.PaymentConfirmRequest): PaymentApprovalResponse {
                throw UnsupportedOperationException()
            }

            override fun cancelPayment(request: com.api.app.dto.request.payment.PaymentConfirmRequest) {
                throw UnsupportedOperationException()
            }

            override fun cancelPaymentByOrder(request: PaymentCancelRequest) {
                capturedCancelRequest = request
            }

            override fun getPgType(): PAY005 = PAY005.NICE
        }

        given(orderBaseTrxRepository.generateClaimNo()).willReturn("C001")
        given(orderDetailRepository.findByIdOrderNoAndIdOrderSequenceAndIdOrderProcessSequence("O001", 1L, 1L))
            .willReturn(originalDetail)
        given(orderGoodsRepository.findByIdOrderNoAndIdGoodsNoAndIdItemNo("O001", "G001", "I001"))
            .willReturn(orderGoods())
        given(payBaseRepository.findByOrderNo("O001")).willReturn(listOf(originalPayment))
        given(payBaseTrxRepository.save(any(PayBase::class.java))).willAnswer {
            (it.arguments[0] as PayBase).apply { payNo = "P-CANCEL-1" }
        }
        given(paymentGatewayFactory.getStrategy(PAY005.NICE)).willReturn(capturingGatewayStrategy)
        doReturn("""{"cancel":true}""").`when`(objectMapper).writeValueAsString(any(PaymentCancelRequest::class.java))
        doAnswer {
            savedCancelDetail = it.arguments[0] as OrderDetail
            it.arguments[0]
        }.`when`(orderDetailTrxRepository).save(any(OrderDetail::class.java))
        doAnswer {
            savedInterfaceLog = it.arguments[0] as PayInterfaceLog
            it.arguments[0]
        }.`when`(payInterfaceLogTrxRepository).save(any(PayInterfaceLog::class.java))

        claimService.cancelOrder(request)

        assertThat(capturedCancelRequest).isNotNull
        assertThat(capturedCancelRequest!!.cancelAmount).isEqualTo(20000L)
        verify(payBaseTrxRepository).updateCancelableAmount("P001", 20000L)
        assertThat(savedCancelDetail).isNotNull
        assertThat(savedCancelDetail!!.claimNo).isEqualTo("C001")
        assertThat(savedInterfaceLog).isNotNull
        assertThat(savedInterfaceLog!!.payNo).isEqualTo("P-CANCEL-1")
    }

    @Test
    @DisplayName("주문 취소 실패 - 원주문 상태 불가")
    fun cancelOrder_Fail_InvalidStatus() {
        given(orderBaseTrxRepository.generateClaimNo()).willReturn("C001")
        given(orderDetailRepository.findByIdOrderNoAndIdOrderSequenceAndIdOrderProcessSequence("O001", 1L, 1L))
            .willReturn(orderDetail(orderStatusCode = ORD002.ORDER_COMPLETED.code))

        assertThatThrownBy { claimService.cancelOrder(cancelRequest()) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("주문접수 상태의 주문만 취소할 수 있습니다")
    }

    @Test
    @DisplayName("주문 취소 실패 - 취소 가능 금액 부족")
    fun cancelOrder_Fail_InsufficientCancelableAmount() {
        given(orderBaseTrxRepository.generateClaimNo()).willReturn("C001")
        given(orderDetailRepository.findByIdOrderNoAndIdOrderSequenceAndIdOrderProcessSequence("O001", 1L, 1L))
            .willReturn(orderDetail())
        given(orderGoodsRepository.findByIdOrderNoAndIdGoodsNoAndIdItemNo("O001", "G001", "I001"))
            .willReturn(orderGoods())
        given(payBaseRepository.findByOrderNo("O001")).willReturn(listOf(cardPayment(cancelableAmount = 5000L)))

        assertThatThrownBy { claimService.cancelOrder(cancelRequest()) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("취소 가능한 금액이 부족합니다")
    }
}
