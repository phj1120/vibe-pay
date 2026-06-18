package com.api.app.service.order

import com.api.app.dto.request.order.OrderRequest
import com.api.app.dto.request.order.PayRequest
import com.api.app.dto.response.basket.BasketResponse
import com.api.app.dto.response.order.CancelableOrderResponse
import com.api.app.dto.response.order.OrderCompleteResponse
import com.api.app.dto.response.order.OrderListResponse
import com.api.app.emum.MEM001
import com.api.app.emum.ORD001
import com.api.app.emum.ORD002
import com.api.app.emum.PAY001
import com.api.app.emum.PAY002
import com.api.app.entity.OrderBase
import com.api.app.entity.OrderDetail
import com.api.app.entity.OrderDetailId
import com.api.app.entity.OrderGoods
import com.api.app.entity.OrderGoodsId
import com.api.app.entity.PayBase
import com.api.app.repository.rodb.goods.GoodsItemRepository
import com.api.app.repository.rodb.goods.GoodsPriceHistRepository
import com.api.app.repository.rodb.member.MemberBaseRepository
import com.api.app.repository.rodb.order.OrderBaseRepository
import com.api.app.repository.rodb.order.OrderGoodsRepository
import com.api.app.repository.rodb.pay.PayBaseRepository
import com.api.app.vo.CancelableOrderItemVo
import com.api.app.vo.OrderCompleteGoodsVo
import com.api.app.vo.OrderCompletePaymentVo
import com.api.app.vo.RefundDetailVo
import com.api.app.repository.rwdb.basket.BasketBaseTrxRepository
import com.api.app.repository.rwdb.order.OrderBaseTrxRepository
import com.api.app.repository.rwdb.order.OrderDetailTrxRepository
import com.api.app.repository.rwdb.order.OrderGoodsTrxRepository
import com.api.app.service.payment.method.PaymentMethodFactory
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class OrderServiceImpl(
    private val orderBaseTrxRepository: OrderBaseTrxRepository,
    private val orderDetailTrxRepository: OrderDetailTrxRepository,
    private val orderGoodsTrxRepository: OrderGoodsTrxRepository,
    private val orderBaseRepository: OrderBaseRepository,
    private val orderGoodsRepository: OrderGoodsRepository,
    private val payBaseRepository: PayBaseRepository,
    private val memberBaseRepository: MemberBaseRepository,
    private val goodsItemRepository: GoodsItemRepository,
    private val goodsPriceHistRepository: GoodsPriceHistRepository,
    private val basketBaseTrxRepository: BasketBaseTrxRepository,
    private val paymentMethodFactory: PaymentMethodFactory
) : OrderService {

    private val log = LoggerFactory.getLogger(this::class.java)

    @Transactional
    override fun generateOrderNumber(): String {
        val orderNo = orderBaseTrxRepository.generateOrderNo()
        log.info("Order number generated: {}", orderNo)
        return orderNo
    }

    @Transactional
    override fun createOrder(request: OrderRequest) {
        log.info("Order creation started. memberNo={}, orderNo={}", request.memberNo, request.orderNo)

        val orderNo = request.orderNo
        val completedPayments = mutableListOf<PayBase>()

        try {
            validateOrder(request)
            processPayments(request, orderNo, completedPayments)
            createOrderEntities(request, orderNo)
            postProcess(request)
            log.info("Order creation completed successfully. orderNo={}", orderNo)
        } catch (e: Exception) {
            log.error("Order creation failed. Rolling back payments. orderNo={}", orderNo, e)
            rollbackCompletedPayments(completedPayments)
            throw e
        }
    }

    private fun validateOrder(request: OrderRequest) {
        val member = memberBaseRepository.findByMemberNo(request.memberNo!!)
            ?: throw IllegalArgumentException("회원 정보를 찾을 수 없습니다")
        if (member.memberStatusCode != MEM001.ACTIVE.code) {
            throw IllegalArgumentException("정상 회원만 주문할 수 있습니다")
        }

        validatePaymentRequests(request)

        val totalOrderAmount = request.goodsList.sumOf { goods ->
            val quantity = goods.quantity ?: 0L
            val salePrice = goods.salePrice ?: 0L
            quantity * salePrice
        }
        val totalPaymentAmount = request.payList.sumOf { it.amount }
        if (totalOrderAmount != totalPaymentAmount) {
            throw IllegalArgumentException("주문 금액과 결제 금액이 일치하지 않습니다")
        }

        request.goodsList.forEach { goods ->
            val item = goodsItemRepository.findByIdGoodsNoAndIdItemNo(goods.goodsNo!!, goods.itemNo!!)
                ?: throw IllegalArgumentException("상품 정보를 찾을 수 없습니다: ${goods.goodsNo}-${goods.itemNo}")
            if (item.stock < (goods.quantity ?: 0L)) {
                throw IllegalArgumentException("재고가 부족합니다: ${goods.goodsName} (재고: ${item.stock}, 요청: ${goods.quantity})")
            }

            val priceHist = goodsPriceHistRepository.selectCurrentPrice(goods.goodsNo!!)
                ?: throw IllegalArgumentException("상품 가격 정보를 찾을 수 없습니다: ${goods.goodsNo}")
            val expectedPrice = priceHist.salePrice + item.itemPrice
            if (expectedPrice != goods.salePrice) {
                throw IllegalArgumentException("가격이 변경되었습니다: ${goods.goodsName} (DB: $expectedPrice, 요청: ${goods.salePrice})")
            }
        }
    }

    private fun validatePaymentRequests(request: OrderRequest) {
        if (request.payList.any { it.amount <= 0 }) {
            throw IllegalArgumentException("결제 금액은 0보다 커야 합니다")
        }
        if (request.payList.any { it.payTypeCode != PAY001.PAYMENT.code }) {
            throw IllegalArgumentException("주문 생성에서는 결제 유형만 사용할 수 있습니다")
        }
        if (request.payList.distinctBy { it.payWayCode }.size != request.payList.size) {
            throw IllegalArgumentException("동일한 결제 수단은 한 번만 사용할 수 있습니다")
        }

        request.payList.forEach { payRequest ->
            when (payRequest.payWayCode) {
                PAY002.CREDIT_CARD.code -> {
                    val confirmRequest = payRequest.paymentConfirmRequest
                        ?: throw IllegalArgumentException("카드 결제 승인 정보가 필요합니다")
                    if (confirmRequest.orderNo != request.orderNo) {
                        throw IllegalArgumentException("결제 승인 정보의 주문번호가 일치하지 않습니다")
                    }
                }
                PAY002.POINT.code -> {
                    if (payRequest.paymentConfirmRequest != null) {
                        throw IllegalArgumentException("포인트 결제에는 카드 승인 정보가 필요하지 않습니다")
                    }
                }
                else -> throw IllegalArgumentException("지원하지 않는 결제 수단입니다: ${payRequest.payWayCode}")
            }
        }
    }

    private fun processPayments(request: OrderRequest, orderNo: String, completedPayments: MutableList<PayBase>) {
        val sortedPayments = request.payList.sortedBy { payReq ->
            PAY002.findByCode(payReq.payWayCode)?.displaySequence ?: Int.MAX_VALUE
        }

        for (payRequest in sortedPayments) {
            try {
                val strategy = paymentMethodFactory.getStrategy(payRequest.payWayCode)
                val payBase = strategy.processPayment(request.memberNo!!, orderNo, payRequest)
                completedPayments.add(payBase)
                log.info("Payment processed successfully. payWayCode={}, amount={}", payRequest.payWayCode, payRequest.amount)
            } catch (e: Exception) {
                log.error("Payment failed. payWayCode={}", payRequest.payWayCode, e)
                rollbackCompletedPayments(completedPayments)
                throw RuntimeException("결제 처리에 실패했습니다: ${e.message}", e)
            }
        }
    }

    private fun rollbackCompletedPayments(completedPayments: List<PayBase>) {
        for (i in completedPayments.indices.reversed()) {
            val payBase = completedPayments[i]
            if (payBase.payWayCode == PAY002.CREDIT_CARD.code) {
                try {
                    log.warn("Initiating network cancellation for payment: {}", payBase.payNo)
                    // TODO: PG사 망취소 API 호출
                    log.warn("Network cancellation completed (TODO): {}", payBase.payNo)
                } catch (e: Exception) {
                    log.error("Network cancellation failed for payment: {}", payBase.payNo, e)
                }
            }
        }
    }

    private fun createOrderEntities(request: OrderRequest, orderNo: String) {
        val now = LocalDateTime.now()

        val orderBase = OrderBase().apply {
            this.orderNo = orderNo
            this.memberNo = request.memberNo!!
        }
        orderBaseTrxRepository.save(orderBase)

        request.goodsList.forEachIndexed { index, goods ->
            val orderSequence = (index + 1).toLong()

            val orderDetail = OrderDetail().apply {
                this.id = OrderDetailId(orderNo = orderNo, orderSequence = orderSequence, orderProcessSequence = 1L)
                this.goodsNo = goods.goodsNo!!
                this.itemNo = goods.itemNo!!
                this.quantity = goods.quantity ?: 1L
                this.orderStatusCode = ORD002.ORDER_RECEIVED.code
                this.deliveryTypeCode = "001"
                this.orderTypeCode = ORD001.ORDER.code
                this.orderAcceptDtm = now
            }
            orderDetailTrxRepository.save(orderDetail)

            val priceHist = goodsPriceHistRepository.selectCurrentPrice(goods.goodsNo!!)

            val orderGoods = OrderGoods().apply {
                this.id = OrderGoodsId(orderNo = orderNo, goodsNo = goods.goodsNo!!, itemNo = goods.itemNo!!)
                this.salePrice = goods.salePrice ?: 0L
                this.supplyPrice = priceHist?.supplyPrice ?: 0L
                this.goodsName = goods.goodsName ?: ""
                this.itemName = goods.itemName ?: ""
            }
            orderGoodsTrxRepository.save(orderGoods)
        }
    }

    private fun postProcess(request: OrderRequest) {
        request.goodsList.forEach { goods ->
            if (goods.basketNo != null) {
                val result = basketBaseTrxRepository.updateBasketIsOrder(goods.basketNo!!, request.memberNo!!)
                if (result != 1) {
                    log.warn("Failed to update basket is_order. basketNo={}", goods.basketNo)
                }
            }
        }
    }

    override fun getOrderComplete(orderNo: String, memberNo: String): OrderCompleteResponse {
        val header = orderBaseRepository.selectOrderCompleteByOrderNo(orderNo, memberNo)
            ?: throw IllegalArgumentException("주문 정보를 찾을 수 없습니다")

        val goodsList = orderGoodsRepository.selectOrderCompleteGoodsByOrderNo(orderNo)
            .map { it.toOrderCompleteGoods() }

        val paymentList = payBaseRepository.selectOrderCompletePaymentByOrderNo(orderNo)
            .map { it.toOrderCompletePayment() }

        return OrderCompleteResponse(
            orderNo = header.orderNo,
            memberNo = header.memberNo,
            orderAcceptDtm = header.orderAcceptDtm,
            totalAmount = header.totalAmount,
            goodsList = goodsList,
            paymentList = paymentList
        )
    }

    override fun getOrderList(memberNo: String): List<OrderListResponse> {
        val flatList = orderBaseRepository.selectOrderListByMemberNo(memberNo)

        return flatList.groupBy { it.orderNo }.map { (orderNo, rows) ->
            val first = rows.first()
            OrderListResponse(
                orderNo = orderNo,
                orderAcceptDtm = first.orderAcceptDtm,
                totalAmount = first.totalAmount,
                goodsList = rows.map { row ->
                    OrderListResponse.OrderListGoods(
                        orderSequence = row.orderSequence,
                        orderProcessSequence = row.orderProcessSequence,
                        goodsNo = row.goodsNo,
                        itemNo = row.itemNo,
                        goodsName = row.goodsName,
                        itemName = row.itemName,
                        salePrice = row.salePrice,
                        quantity = row.quantity,
                        orderStatusCode = row.orderStatusCode,
                        orderStatusName = row.orderStatusName,
                        orderTypeCode = row.orderTypeCode,
                        orderTypeName = row.orderTypeName,
                        cancelable = row.cancelable,
                        cancelableAmount = row.cancelableAmount
                    )
                }
            )
        }
    }

    override fun getCancelableOrders(orderNo: String, memberNo: String): CancelableOrderResponse {
        val orderBase = orderBaseRepository.findById(orderNo).orElse(null)
        if (orderBase == null || orderBase.memberNo != memberNo) {
            throw IllegalArgumentException("주문 정보를 찾을 수 없거나 접근 권한이 없습니다")
        }

        val cancelableItems = orderBaseRepository.selectCancelableOrdersByOrderNo(orderNo, memberNo)
        if (cancelableItems.isEmpty()) throw IllegalArgumentException("취소 가능한 상품이 없습니다")

        val refundDetails = orderBaseRepository.selectRefundDetailsByOrderNo(orderNo)
        val totalRefundAmount = refundDetails.sumOf { it.refundAmount ?: 0L }

        return CancelableOrderResponse(
            orderNo = orderNo,
            cancelableItems = cancelableItems.map { it.toCancelableItem() },
            refundInfo = CancelableOrderResponse.RefundInfo(
                totalRefundAmount = totalRefundAmount,
                refundDetails = refundDetails.map { it.toRefundDetail() }
            )
        )
    }

    private fun OrderCompleteGoodsVo.toOrderCompleteGoods() = OrderCompleteResponse.OrderCompleteGoods(
        goodsNo = goodsNo,
        itemNo = itemNo,
        goodsName = goodsName,
        itemName = itemName,
        salePrice = salePrice,
        quantity = quantity,
        subtotal = subtotal
    )

    private fun OrderCompletePaymentVo.toOrderCompletePayment() = OrderCompleteResponse.OrderCompletePayment(
        payWayCode = payWayCode,
        payWayName = when (payWayCode) {
            "001" -> "신용카드"
            "002" -> "포인트"
            else -> ""
        },
        amount = amount,
        pgTypeCode = pgTypeCode,
        pgTypeName = when (pgTypeCode) {
            "001" -> "이니시스"
            "002" -> "나이스"
            "999" -> "테스트PG"
            else -> ""
        }
    )

    private fun CancelableOrderItemVo.toCancelableItem() = CancelableOrderResponse.CancelableOrderItem(
        orderSequence = orderSequence,
        orderProcessSequence = orderProcessSequence,
        goodsNo = goodsNo,
        itemNo = itemNo,
        goodsName = goodsName,
        itemName = itemName,
        salePrice = salePrice,
        quantity = quantity,
        subtotal = subtotal
    )

    private fun RefundDetailVo.toRefundDetail() = CancelableOrderResponse.RefundDetail(
        payWayCode = payWayCode,
        payWayName = payWayName,
        refundAmount = refundAmount,
        pgTypeCode = pgTypeCode,
        pgTypeName = pgTypeName
    )
}
