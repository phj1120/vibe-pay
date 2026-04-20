package com.api.app.service.order

import com.api.app.client.CoreFeignClient
import com.api.app.client.GoodsFeignClient
import com.api.app.client.dto.GoodsItemSnapshot
import com.api.app.client.dto.GoodsValidateItem
import com.api.app.client.dto.GoodsValidateRequest
import com.api.app.client.dto.BasketResponse
import com.api.app.dto.request.order.OrderRequest
import com.api.app.dto.response.order.CancelableOrderResponse
import com.api.app.dto.response.order.OrderCompleteResponse
import com.api.app.dto.response.order.OrderListResponse
import com.api.app.emum.MEM001
import com.api.app.emum.ORD001
import com.api.app.emum.ORD002
import com.api.app.entity.OrderBase
import com.api.app.entity.OrderDetail
import com.api.app.entity.OrderDetailId
import com.api.app.entity.OrderGoods
import com.api.app.entity.OrderGoodsId
import com.api.app.repository.rodb.order.CancelableOrderItemProjection
import com.api.app.repository.rodb.order.OrderBaseRepository
import com.api.app.repository.rodb.order.OrderCompleteGoodsProjection
import com.api.app.repository.rodb.order.OrderGoodsRepository
import com.api.app.repository.rodb.order.RefundDetailProjection
import com.api.app.repository.rodb.pay.OrderCompletePaymentProjection
import com.api.app.repository.rodb.pay.PayBaseRepository
import com.api.app.repository.rwdb.order.OrderBaseTrxRepository
import com.api.app.repository.rwdb.order.OrderDetailTrxRepository
import com.api.app.repository.rwdb.order.OrderGoodsTrxRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.vibepay.messaging.EventEnvelope
import com.vibepay.messaging.KafkaTopics
import com.vibepay.messaging.OutboxMessage
import com.vibepay.messaging.OutboxRepository
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
    private val goodsFeignClient: GoodsFeignClient,
    private val coreFeignClient: CoreFeignClient,
    private val outboxRepository: OutboxRepository,
    private val objectMapper: ObjectMapper
) : OrderService {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun generateOrderNumber(): String {
        val orderNo = orderBaseTrxRepository.generateOrderNo()
        log.info("Order number generated: {}", orderNo)
        return orderNo
    }

    @Transactional
    override fun createOrder(request: OrderRequest) {
        log.info("Order creation started. memberNo={}, orderNo={}", request.memberNo, request.orderNo)
        val orderNo = request.orderNo
        val snapshotMap = validateOrder(request)
        createOrderEntities(request, orderNo, snapshotMap)
        saveOrderCreatedOutbox(request)
        log.info("Order created (PENDING). orderNo={}", orderNo)
    }

    @Transactional
    fun handlePaymentApproved(orderNo: String, memberNo: String, basketNos: List<String>, earnPointAmount: Long) {
        orderBaseTrxRepository.updateOrderStatus(orderNo, "PAID")
        saveOutbox(orderNo, "OrderConfirmed", KafkaTopics.ORDER_EVENTS, mapOf(
            "orderNo" to orderNo, "memberNo" to memberNo,
            "basketNos" to basketNos, "earnPointAmount" to earnPointAmount
        ))
        log.info("Order PAID and OrderConfirmed outbox saved. orderNo={}", orderNo)
    }

    @Transactional
    fun handlePaymentFailed(orderNo: String, memberNo: String, reason: String) {
        orderBaseTrxRepository.updateOrderStatus(orderNo, "CANCELLED")
        log.warn("Order CANCELLED due to payment failure. orderNo={}, reason={}", orderNo, reason)
    }

    private fun validateOrder(request: OrderRequest): Map<String, GoodsItemSnapshot> {
        val memberResponse = coreFeignClient.getMemberByNo(request.memberNo!!).data
            ?: throw IllegalArgumentException("회원 정보를 찾을 수 없습니다")
        if (memberResponse.memberStatusCode != MEM001.ACTIVE.code) {
            throw IllegalArgumentException("정상 회원만 주문할 수 있습니다")
        }
        val validateItems = request.goodsList.map { goods ->
            GoodsValidateItem(goodsNo = goods.goodsNo!!, itemNo = goods.itemNo!!, quantity = goods.quantity ?: 1L, expectedSalePrice = goods.salePrice ?: 0L)
        }
        val result = goodsFeignClient.validateGoods(GoodsValidateRequest(validateItems)).data
            ?: throw IllegalStateException("상품 검증 결과를 받지 못했습니다")
        if (!result.valid) throw IllegalArgumentException(result.errorMessage ?: "상품 검증 실패")
        return result.items.associateBy { "${it.goodsNo}-${it.itemNo}" }
    }

    private fun createOrderEntities(request: OrderRequest, orderNo: String, snapshotMap: Map<String, GoodsItemSnapshot>) {
        val now = LocalDateTime.now()
        val orderBase = OrderBase().apply { this.orderNo = orderNo; this.memberNo = request.memberNo!!; orderStatusCode = "PENDING" }
        orderBaseTrxRepository.save(orderBase)

        request.goodsList.forEachIndexed { index, goods ->
            val seq = (index + 1).toLong()
            val snapshot = snapshotMap["${goods.goodsNo}-${goods.itemNo}"]
            orderDetailTrxRepository.save(OrderDetail().apply {
                this.id = OrderDetailId(orderNo = orderNo, orderSequence = seq, orderProcessSequence = 1L)
                this.goodsNo = goods.goodsNo!!; this.itemNo = goods.itemNo!!
                this.quantity = goods.quantity ?: 1L; this.orderStatusCode = ORD002.ORDER_RECEIVED.code
                this.deliveryTypeCode = "001"; this.orderTypeCode = ORD001.ORDER.code; this.orderAcceptDtm = now
            })
            orderGoodsTrxRepository.save(OrderGoods().apply {
                this.id = OrderGoodsId(orderNo = orderNo, goodsNo = goods.goodsNo!!, itemNo = goods.itemNo!!)
                this.salePrice = goods.salePrice ?: 0L; this.supplyPrice = snapshot?.supplyPrice ?: 0L
                this.goodsName = snapshot?.goodsName ?: goods.goodsName ?: ""
                this.itemName = snapshot?.itemName ?: goods.itemName ?: ""
            })
        }
    }

    private fun saveOrderCreatedOutbox(request: OrderRequest) {
        val payItems = request.payList.map { pay ->
            val confirmReq = pay.paymentConfirmRequest
            mapOf(
                "payWayCode" to pay.payWayCode, "amount" to pay.amount, "payTypeCode" to pay.payTypeCode,
                "pgTypeCode" to confirmReq?.pgTypeCode, "authToken" to confirmReq?.authToken,
                "authUrl" to confirmReq?.authUrl, "netCancelUrl" to confirmReq?.netCancelUrl,
                "price" to confirmReq?.price, "transactionId" to confirmReq?.transactionId,
                "niceAmount" to confirmReq?.amount, "tradeNo" to confirmReq?.tradeNo, "mid" to confirmReq?.mid
            )
        }
        val payload = mapOf(
            "orderNo" to (request.orderNo ?: ""),
            "memberNo" to (request.memberNo ?: ""),
            "payItems" to payItems,
            "basketNos" to request.goodsList.mapNotNull { it.basketNo }
        )
        saveOutbox(request.orderNo ?: "", "OrderCreated", KafkaTopics.ORDER_EVENTS, payload)
    }

    private fun saveOutbox(aggregateId: String, eventType: String, topic: String, payload: Map<String, Any?>) {
        try {
            val envelope = EventEnvelope(type = eventType, aggregateType = "Order", aggregateId = aggregateId, payload = payload)
            outboxRepository.save(OutboxMessage(aggregateType = "Order", aggregateId = aggregateId, eventType = eventType, topic = topic, payload = objectMapper.writeValueAsString(envelope)))
        } catch (e: Exception) {
            log.error("Failed to save outbox message. eventType={}, aggregateId={}", eventType, aggregateId, e)
        }
    }

    override fun getOrderComplete(orderNo: String, memberNo: String): OrderCompleteResponse {
        val headerProjection = orderBaseRepository.selectOrderCompleteByOrderNo(orderNo, memberNo)
            ?: throw IllegalArgumentException("주문 정보를 찾을 수 없습니다")
        val goodsList = orderGoodsRepository.selectOrderCompleteGoodsByOrderNo(orderNo).map { it.toOrderCompleteGoods() }
        val paymentList = payBaseRepository.selectOrderCompletePaymentByOrderNo(orderNo).map { it.toOrderCompletePayment() }
        return OrderCompleteResponse(
            orderNo = headerProjection.getOrderNo(), memberNo = headerProjection.getMemberNo(),
            orderAcceptDtm = headerProjection.getOrderAcceptDtm(), totalAmount = headerProjection.getTotalAmount(),
            goodsList = goodsList, paymentList = paymentList
        )
    }

    override fun getOrderList(memberNo: String): List<OrderListResponse> {
        val flatList = orderBaseRepository.selectOrderListByMemberNo(memberNo)
        return flatList.groupBy { it.getOrderNo() }.map { (orderNo, rows) ->
            val first = rows.first()
            OrderListResponse(
                orderNo = orderNo, orderAcceptDtm = first.getOrderAcceptDtm(), totalAmount = first.getTotalAmount(),
                goodsList = rows.map { row ->
                    OrderListResponse.OrderListGoods(
                        orderSequence = row.getOrderSequence(), orderProcessSequence = row.getOrderProcessSequence(),
                        goodsNo = row.getGoodsNo(), itemNo = row.getItemNo(), goodsName = row.getGoodsName(),
                        itemName = row.getItemName(), salePrice = row.getSalePrice(), quantity = row.getQuantity(),
                        orderStatusCode = row.getOrderStatusCode(), orderStatusName = row.getOrderStatusName(),
                        orderTypeCode = row.getOrderTypeCode(), orderTypeName = row.getOrderTypeName(),
                        cancelable = row.getCancelable(), cancelableAmount = row.getCancelableAmount()
                    )
                }
            )
        }
    }

    override fun getCancelableOrders(orderNo: String, memberNo: String): CancelableOrderResponse {
        val orderBase = orderBaseRepository.findById(orderNo).orElse(null)
        if (orderBase == null || orderBase.memberNo != memberNo) throw IllegalArgumentException("주문 정보를 찾을 수 없거나 접근 권한이 없습니다")
        val cancelableItems = orderBaseRepository.selectCancelableOrdersByOrderNo(orderNo, memberNo)
        if (cancelableItems.isEmpty()) throw IllegalArgumentException("취소 가능한 상품이 없습니다")
        val refundDetails = orderBaseRepository.selectRefundDetailsByOrderNo(orderNo)
        return CancelableOrderResponse(
            orderNo = orderNo, cancelableItems = cancelableItems.map { it.toCancelableItem() },
            refundInfo = CancelableOrderResponse.RefundInfo(
                totalRefundAmount = refundDetails.sumOf { it.getRefundAmount() ?: 0L },
                refundDetails = refundDetails.map { it.toRefundDetail() }
            )
        )
    }

    private fun OrderCompleteGoodsProjection.toOrderCompleteGoods() = OrderCompleteResponse.OrderCompleteGoods(
        goodsNo = getGoodsNo(), itemNo = getItemNo(), goodsName = getGoodsName(),
        itemName = getItemName(), salePrice = getSalePrice(), quantity = getQuantity(), subtotal = getSubtotal()
    )
    private fun OrderCompletePaymentProjection.toOrderCompletePayment() = OrderCompleteResponse.OrderCompletePayment(
        payWayCode = getPayWayCode(), payWayName = getPayWayName(), amount = getAmount(),
        pgTypeCode = getPgTypeCode(), pgTypeName = getPgTypeName()
    )
    private fun CancelableOrderItemProjection.toCancelableItem() = CancelableOrderResponse.CancelableOrderItem(
        orderSequence = getOrderSequence(), orderProcessSequence = getOrderProcessSequence(),
        goodsNo = getGoodsNo(), itemNo = getItemNo(), goodsName = getGoodsName(),
        itemName = getItemName(), salePrice = getSalePrice(), quantity = getQuantity(), subtotal = getSubtotal()
    )
    private fun RefundDetailProjection.toRefundDetail() = CancelableOrderResponse.RefundDetail(
        payWayCode = getPayWayCode(), payWayName = getPayWayName(), refundAmount = getRefundAmount(),
        pgTypeCode = getPgTypeCode(), pgTypeName = getPgTypeName()
    )
}
