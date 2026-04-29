package com.api.app.service.order

import com.api.app.dto.request.order.OrderRequest
import com.api.app.dto.request.order.PayRequest
import com.api.app.dto.request.payment.PaymentConfirmRequest
import com.api.app.dto.response.basket.BasketResponse
import com.api.app.emum.MEM001
import com.api.app.emum.ORD001
import com.api.app.emum.PAY001
import com.api.app.emum.PAY002
import com.api.app.emum.PAY005
import com.api.app.entity.GoodsItem
import com.api.app.entity.GoodsItemId
import com.api.app.entity.GoodsPriceHist
import com.api.app.entity.MemberBase
import com.api.app.entity.OrderBase
import com.api.app.entity.OrderDetail
import com.api.app.entity.OrderGoods
import com.api.app.entity.PayBase
import com.api.app.repository.rodb.goods.GoodsItemRepository
import com.api.app.repository.rodb.goods.GoodsPriceHistRepository
import com.api.app.repository.rodb.member.MemberBaseRepository
import com.api.app.repository.rodb.order.CancelableOrderItemProjection
import com.api.app.repository.rodb.order.OrderBaseRepository
import com.api.app.repository.rodb.order.OrderCompleteHeaderProjection
import com.api.app.repository.rodb.order.OrderCompleteGoodsProjection
import com.api.app.repository.rodb.order.OrderGoodsRepository
import com.api.app.repository.rodb.order.OrderListFlatProjection
import com.api.app.repository.rodb.order.RefundDetailProjection
import com.api.app.repository.rodb.pay.OrderCompletePaymentProjection
import com.api.app.repository.rodb.pay.PayBaseRepository
import com.api.app.repository.rwdb.basket.BasketBaseTrxRepository
import com.api.app.repository.rwdb.order.OrderBaseTrxRepository
import com.api.app.repository.rwdb.order.OrderDetailTrxRepository
import com.api.app.repository.rwdb.order.OrderGoodsTrxRepository
import com.api.app.service.payment.method.PaymentMethodFactory
import com.api.app.service.payment.method.PaymentMethodStrategy
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.lenient
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import java.time.LocalDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceImplTest {

    @InjectMocks
    private lateinit var orderService: OrderServiceImpl

    @Mock
    private lateinit var orderBaseTrxRepository: OrderBaseTrxRepository

    @Mock
    private lateinit var orderDetailTrxRepository: OrderDetailTrxRepository

    @Mock
    private lateinit var orderGoodsTrxRepository: OrderGoodsTrxRepository

    @Mock
    private lateinit var orderBaseRepository: OrderBaseRepository

    @Mock
    private lateinit var orderGoodsRepository: OrderGoodsRepository

    @Mock
    private lateinit var payBaseRepository: PayBaseRepository

    @Mock
    private lateinit var memberBaseRepository: MemberBaseRepository

    @Mock
    private lateinit var goodsItemRepository: GoodsItemRepository

    @Mock
    private lateinit var goodsPriceHistRepository: GoodsPriceHistRepository

    @Mock
    private lateinit var basketBaseTrxRepository: BasketBaseTrxRepository

    @Mock
    private lateinit var paymentMethodFactory: PaymentMethodFactory

    @Mock
    private lateinit var cardStrategy: PaymentMethodStrategy

    @Mock
    private lateinit var pointStrategy: PaymentMethodStrategy

    @Test
    @DisplayName("createOrder processes payments in display order and persists order data")
    fun createOrderProcessesPaymentsInDisplayOrderAndPersistsOrderData() {
        val request = baseOrderRequest(
            payList = listOf(
                PayRequest(PAY002.POINT.code, 3000L, PAY001.PAYMENT.code),
                PayRequest(
                    PAY002.CREDIT_CARD.code,
                    7000L,
                    PAY001.PAYMENT.code,
                    PaymentConfirmRequest(pgTypeCode = PAY005.INICIS.code, orderNo = "20260428O000001")
                )
            )
        )
        val member = activeMember()
        val goodsItem = goodsItem(stock = 10L, itemPrice = 100L)
        val priceHist = goodsPriceHist(salePrice = 9900L, supplyPrice = 7000L)
        val cardPayBase = PayBase().apply {
            payNo = "000000000001001"
            payWayCode = PAY002.CREDIT_CARD.code
        }
        val pointPayBase = PayBase().apply {
            payNo = "000000000001002"
            payWayCode = PAY002.POINT.code
        }

        given(memberBaseRepository.findByMemberNo("000000000000001")).willReturn(member)
        given(goodsItemRepository.findByIdGoodsNoAndIdItemNo("G-1", "I01")).willReturn(goodsItem)
        given(goodsPriceHistRepository.selectCurrentPrice("G-1")).willReturn(priceHist)
        given(paymentMethodFactory.getStrategy(PAY002.CREDIT_CARD.code)).willReturn(cardStrategy)
        given(paymentMethodFactory.getStrategy(PAY002.POINT.code)).willReturn(pointStrategy)
        given(cardStrategy.processPayment("000000000000001", "20260428O000001", request.payList[1])).willReturn(cardPayBase)
        given(pointStrategy.processPayment("000000000000001", "20260428O000001", request.payList[0])).willReturn(pointPayBase)
        given(basketBaseTrxRepository.updateBasketIsOrder("BASKET-1", "000000000000001")).willReturn(1)

        orderService.createOrder(request)

        val paymentOrder = inOrder(paymentMethodFactory, cardStrategy, pointStrategy)
        paymentOrder.verify(paymentMethodFactory).getStrategy(PAY002.CREDIT_CARD.code)
        paymentOrder.verify(cardStrategy).processPayment("000000000000001", "20260428O000001", request.payList[1])
        paymentOrder.verify(paymentMethodFactory).getStrategy(PAY002.POINT.code)
        paymentOrder.verify(pointStrategy).processPayment("000000000000001", "20260428O000001", request.payList[0])

        val orderBaseCaptor = ArgumentCaptor.forClass(OrderBase::class.java)
        verify(orderBaseTrxRepository).save(orderBaseCaptor.capture())
        assertThat(orderBaseCaptor.value.orderNo).isEqualTo("20260428O000001")
        assertThat(orderBaseCaptor.value.memberNo).isEqualTo("000000000000001")

        val detailCaptor = ArgumentCaptor.forClass(OrderDetail::class.java)
        verify(orderDetailTrxRepository).save(detailCaptor.capture())
        assertThat(detailCaptor.value.id.orderSequence).isEqualTo(1L)
        assertThat(detailCaptor.value.orderTypeCode).isEqualTo(ORD001.ORDER.code)

        val goodsCaptor = ArgumentCaptor.forClass(OrderGoods::class.java)
        verify(orderGoodsTrxRepository).save(goodsCaptor.capture())
        assertThat(goodsCaptor.value.salePrice).isEqualTo(10000L)
        assertThat(goodsCaptor.value.supplyPrice).isEqualTo(7000L)

        verify(basketBaseTrxRepository).updateBasketIsOrder("BASKET-1", "000000000000001")
    }

    @Test
    @DisplayName("createOrder throws when member is not active")
    fun createOrderThrowsWhenMemberIsNotActive() {
        val request = baseOrderRequest()
        val member = activeMember().apply { memberStatusCode = "002" }
        given(memberBaseRepository.findByMemberNo("000000000000001")).willReturn(member)

        assertThatThrownBy { orderService.createOrder(request) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("정상 회원만 주문할 수 있습니다")

        verify(paymentMethodFactory, never()).getStrategy(org.mockito.ArgumentMatchers.anyString())
    }

    @Test
    @DisplayName("createOrder throws when stock is insufficient")
    fun createOrderThrowsWhenStockIsInsufficient() {
        val request = baseOrderRequest()
        given(memberBaseRepository.findByMemberNo("000000000000001")).willReturn(activeMember())
        given(goodsItemRepository.findByIdGoodsNoAndIdItemNo("G-1", "I01")).willReturn(goodsItem(stock = 0L, itemPrice = 100L))

        assertThatThrownBy { orderService.createOrder(request) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("재고가 부족합니다")

        verify(goodsPriceHistRepository, never()).selectCurrentPrice("G-1")
    }

    @Test
    @DisplayName("createOrder throws when payment total does not match order total")
    fun createOrderThrowsWhenPaymentTotalDoesNotMatchOrderTotal() {
        val request = baseOrderRequest(
            payList = listOf(
                PayRequest(
                    payWayCode = PAY002.CREDIT_CARD.code,
                    amount = 9000L,
                    payTypeCode = PAY001.PAYMENT.code,
                    paymentConfirmRequest = PaymentConfirmRequest(pgTypeCode = PAY005.INICIS.code, orderNo = "20260428O000001")
                )
            )
        )
        given(memberBaseRepository.findByMemberNo("000000000000001")).willReturn(activeMember())

        assertThatThrownBy { orderService.createOrder(request) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("주문 금액과 결제 금액이 일치하지 않습니다")

        verify(goodsItemRepository, never()).findByIdGoodsNoAndIdItemNo("G-1", "I01")
    }

    @Test
    @DisplayName("createOrder throws when card payment approval info is missing")
    fun createOrderThrowsWhenCardApprovalInfoIsMissing() {
        val request = baseOrderRequest(
            payList = listOf(
                PayRequest(
                    payWayCode = PAY002.CREDIT_CARD.code,
                    amount = 10000L,
                    payTypeCode = PAY001.PAYMENT.code,
                    paymentConfirmRequest = null
                )
            )
        )
        given(memberBaseRepository.findByMemberNo("000000000000001")).willReturn(activeMember())

        assertThatThrownBy { orderService.createOrder(request) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("카드 결제 승인 정보가 필요합니다")
    }

    @Test
    @DisplayName("createOrder throws when point payment includes card approval info")
    fun createOrderThrowsWhenPointPaymentIncludesCardApprovalInfo() {
        val request = baseOrderRequest(
            payList = listOf(
                PayRequest(
                    payWayCode = PAY002.POINT.code,
                    amount = 10000L,
                    payTypeCode = PAY001.PAYMENT.code,
                    paymentConfirmRequest = PaymentConfirmRequest(pgTypeCode = PAY005.INICIS.code, orderNo = "20260428O000001")
                )
            )
        )
        given(memberBaseRepository.findByMemberNo("000000000000001")).willReturn(activeMember())

        assertThatThrownBy { orderService.createOrder(request) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("포인트 결제에는 카드 승인 정보가 필요하지 않습니다")
    }

    @Test
    @DisplayName("createOrder throws when duplicate pay ways are provided")
    fun createOrderThrowsWhenDuplicatePayWaysAreProvided() {
        val request = baseOrderRequest(
            payList = listOf(
                PayRequest(
                    payWayCode = PAY002.CREDIT_CARD.code,
                    amount = 7000L,
                    payTypeCode = PAY001.PAYMENT.code,
                    paymentConfirmRequest = PaymentConfirmRequest(pgTypeCode = PAY005.INICIS.code, orderNo = "20260428O000001")
                ),
                PayRequest(
                    payWayCode = PAY002.CREDIT_CARD.code,
                    amount = 3000L,
                    payTypeCode = PAY001.PAYMENT.code,
                    paymentConfirmRequest = PaymentConfirmRequest(pgTypeCode = PAY005.NICE.code, orderNo = "20260428O000001")
                )
            )
        )
        given(memberBaseRepository.findByMemberNo("000000000000001")).willReturn(activeMember())

        assertThatThrownBy { orderService.createOrder(request) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("동일한 결제 수단은 한 번만 사용할 수 있습니다")
    }

    @Test
    @DisplayName("createOrder rolls back flow when payment processing fails")
    fun createOrderRollsBackFlowWhenPaymentProcessingFails() {
        val request = baseOrderRequest()
        given(memberBaseRepository.findByMemberNo("000000000000001")).willReturn(activeMember())
        given(goodsItemRepository.findByIdGoodsNoAndIdItemNo("G-1", "I01")).willReturn(goodsItem(stock = 10L, itemPrice = 100L))
        given(goodsPriceHistRepository.selectCurrentPrice("G-1")).willReturn(goodsPriceHist(salePrice = 9900L, supplyPrice = 7000L))
        given(paymentMethodFactory.getStrategy(PAY002.CREDIT_CARD.code)).willReturn(cardStrategy)
        given(cardStrategy.processPayment("000000000000001", "20260428O000001", request.payList.first()))
            .willThrow(RuntimeException("PG timeout"))

        assertThatThrownBy { orderService.createOrder(request) }
            .isInstanceOf(RuntimeException::class.java)
            .hasMessageContaining("결제 처리에 실패했습니다")

        verify(orderBaseTrxRepository, never()).save(org.mockito.ArgumentMatchers.any(OrderBase::class.java))
        verify(orderDetailTrxRepository, never()).save(org.mockito.ArgumentMatchers.any(OrderDetail::class.java))
    }

    @Test
    @DisplayName("getOrderComplete maps header goods and payments")
    fun getOrderCompleteMapsHeaderGoodsAndPayments() {
        val header = org.mockito.Mockito.mock(OrderCompleteHeaderProjection::class.java).also {
            given(it.getOrderNo()).willReturn("20260428O000001")
            given(it.getMemberNo()).willReturn("000000000000001")
            given(it.getOrderAcceptDtm()).willReturn(LocalDateTime.of(2026, 4, 28, 12, 0))
            given(it.getTotalAmount()).willReturn(10000L)
        }
        val goods = org.mockito.Mockito.mock(OrderCompleteGoodsProjection::class.java).also {
            given(it.getGoodsNo()).willReturn("G-1")
            given(it.getItemNo()).willReturn("I01")
            given(it.getGoodsName()).willReturn("Speaker")
            given(it.getItemName()).willReturn("Black")
            given(it.getSalePrice()).willReturn(10000L)
            given(it.getQuantity()).willReturn(1L)
            given(it.getSubtotal()).willReturn(10000L)
        }
        val payment = org.mockito.Mockito.mock(OrderCompletePaymentProjection::class.java).also {
            given(it.getPayWayCode()).willReturn(PAY002.CREDIT_CARD.code)
            given(it.getPayWayName()).willReturn("Card")
            given(it.getAmount()).willReturn(10000L)
            given(it.getPgTypeCode()).willReturn(PAY005.INICIS.code)
            given(it.getPgTypeName()).willReturn("Inicis")
        }
        given(orderBaseRepository.selectOrderCompleteByOrderNo("20260428O000001", "000000000000001")).willReturn(header)
        given(orderGoodsRepository.selectOrderCompleteGoodsByOrderNo("20260428O000001")).willReturn(listOf(goods))
        given(payBaseRepository.selectOrderCompletePaymentByOrderNo("20260428O000001")).willReturn(listOf(payment))

        val result = orderService.getOrderComplete("20260428O000001", "000000000000001")

        assertThat(result.orderNo).isEqualTo("20260428O000001")
        assertThat(result.totalAmount).isEqualTo(10000L)
        assertThat(result.goodsList).hasSize(1)
        assertThat(result.paymentList).hasSize(1)
    }

    @Test
    @DisplayName("getOrderList groups flat rows by order number")
    fun getOrderListGroupsFlatRowsByOrderNumber() {
        val firstRow = orderListRow("20260428O000001", 1L, "Speaker", 10000L)
        val secondRow = orderListRow("20260428O000001", 2L, "Stand", 5000L)
        given(orderBaseRepository.selectOrderListByMemberNo("000000000000001")).willReturn(listOf(firstRow, secondRow))

        val result = orderService.getOrderList("000000000000001")

        assertThat(result).hasSize(1)
        assertThat(result.first().goodsList).hasSize(2)
        assertThat(result.first().goodsList.map { it.goodsName }).containsExactly("Speaker", "Stand")
    }

    @Test
    @DisplayName("getCancelableOrders returns cancelable items and refund info")
    fun getCancelableOrdersReturnsCancelableItemsAndRefundInfo() {
        val orderBase = OrderBase().apply {
            orderNo = "20260428O000001"
            memberNo = "000000000000001"
        }
        val cancelableItem = org.mockito.Mockito.mock(CancelableOrderItemProjection::class.java).also {
            given(it.getOrderSequence()).willReturn(1L)
            given(it.getOrderProcessSequence()).willReturn(1L)
            given(it.getGoodsNo()).willReturn("G-1")
            given(it.getItemNo()).willReturn("I01")
            given(it.getGoodsName()).willReturn("Speaker")
            given(it.getItemName()).willReturn("Black")
            given(it.getSalePrice()).willReturn(10000L)
            given(it.getQuantity()).willReturn(1L)
            given(it.getSubtotal()).willReturn(10000L)
        }
        val refundDetail = org.mockito.Mockito.mock(RefundDetailProjection::class.java).also {
            given(it.getPayWayCode()).willReturn(PAY002.CREDIT_CARD.code)
            given(it.getPayWayName()).willReturn("Card")
            given(it.getRefundAmount()).willReturn(10000L)
            given(it.getPgTypeCode()).willReturn(PAY005.INICIS.code)
            given(it.getPgTypeName()).willReturn("Inicis")
        }
        given(orderBaseRepository.findById("20260428O000001")).willReturn(Optional.of(orderBase))
        given(orderBaseRepository.selectCancelableOrdersByOrderNo("20260428O000001", "000000000000001"))
            .willReturn(listOf(cancelableItem))
        given(orderBaseRepository.selectRefundDetailsByOrderNo("20260428O000001")).willReturn(listOf(refundDetail))

        val result = orderService.getCancelableOrders("20260428O000001", "000000000000001")

        assertThat(result.cancelableItems).hasSize(1)
        assertThat(result.refundInfo?.totalRefundAmount).isEqualTo(10000L)
        assertThat(result.refundInfo?.refundDetails).hasSize(1)
    }

    private fun activeMember() = MemberBase().apply {
        memberNo = "000000000000001"
        memberStatusCode = MEM001.ACTIVE.code
    }

    private fun goodsItem(stock: Long, itemPrice: Long) = GoodsItem().apply {
        id = GoodsItemId(goodsNo = "G-1", itemNo = "I01")
        this.stock = stock
        this.itemPrice = itemPrice
    }

    private fun goodsPriceHist(salePrice: Long, supplyPrice: Long) = GoodsPriceHist().apply {
        this.salePrice = salePrice
        this.supplyPrice = supplyPrice
    }

    private fun baseOrderRequest(
        payList: List<PayRequest> = listOf(
            PayRequest(
                payWayCode = PAY002.CREDIT_CARD.code,
                amount = 10000L,
                payTypeCode = PAY001.PAYMENT.code,
                paymentConfirmRequest = PaymentConfirmRequest(pgTypeCode = PAY005.INICIS.code, orderNo = "20260428O000001")
            )
        )
    ) = OrderRequest(
        orderNo = "20260428O000001",
        memberNo = "000000000000001",
        memberName = "Kim",
        phone = "01012341234",
        email = "kim@test.com",
        goodsList = listOf(
            BasketResponse(
                basketNo = "BASKET-1",
                goodsNo = "G-1",
                goodsName = "Speaker",
                salePrice = 10000L,
                itemNo = "I01",
                itemName = "Black",
                quantity = 1L
            )
        ),
        payList = payList
    )

    private fun orderListRow(
        orderNo: String,
        orderSequence: Long,
        goodsName: String,
        salePrice: Long
    ) = org.mockito.Mockito.mock(OrderListFlatProjection::class.java).also {
        given(it.getOrderNo()).willReturn(orderNo)
        lenient().`when`(it.getOrderAcceptDtm()).thenReturn(LocalDateTime.of(2026, 4, 28, 12, 0))
        lenient().`when`(it.getTotalAmount()).thenReturn(15000L)
        given(it.getOrderSequence()).willReturn(orderSequence)
        given(it.getOrderProcessSequence()).willReturn(1L)
        given(it.getGoodsNo()).willReturn("G-$orderSequence")
        given(it.getItemNo()).willReturn("I0$orderSequence")
        given(it.getGoodsName()).willReturn(goodsName)
        given(it.getItemName()).willReturn("Item-$orderSequence")
        given(it.getSalePrice()).willReturn(salePrice)
        given(it.getQuantity()).willReturn(1L)
        given(it.getOrderStatusCode()).willReturn("001")
        given(it.getOrderStatusName()).willReturn("Received")
        given(it.getOrderTypeCode()).willReturn("001")
        given(it.getOrderTypeName()).willReturn("Order")
        given(it.getCancelable()).willReturn(true)
        given(it.getCancelableAmount()).willReturn(15000L)
    }
}
