package com.api.app.controller.order

import com.api.app.common.response.ApiResponse
import com.api.app.dto.internal.CancelableItemDto
import com.api.app.dto.internal.CreateCancelDetailsRequest
import com.api.app.dto.internal.OrderCancelInfoResponse
import com.api.app.dto.internal.PaymentInfoDto
import com.api.app.emum.DLV001
import com.api.app.emum.ORD001
import com.api.app.emum.ORD002
import com.api.app.emum.PAY002
import com.api.app.entity.OrderDetail
import com.api.app.entity.OrderDetailId
import com.api.app.repository.rodb.order.OrderDetailRepository
import com.api.app.repository.rodb.order.OrderGoodsRepository
import com.api.app.repository.rodb.pay.PayBaseRepository
import com.api.app.repository.rwdb.order.OrderBaseTrxRepository
import com.api.app.repository.rwdb.order.OrderDetailTrxRepository
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/internal/orders")
class OrderInternalController(
    private val orderDetailRepository: OrderDetailRepository,
    private val orderGoodsRepository: OrderGoodsRepository,
    private val payBaseRepository: PayBaseRepository,
    private val orderBaseTrxRepository: OrderBaseTrxRepository,
    private val orderDetailTrxRepository: OrderDetailTrxRepository
) {

    @GetMapping("/{orderNo}/cancel-info")
    fun getOrderCancelInfo(
        @PathVariable orderNo: String,
        @RequestParam memberNo: String
    ): ApiResponse<OrderCancelInfoResponse> {
        val orderDetails = orderDetailRepository.findByIdOrderNo(orderNo)
            .filter { it.orderTypeCode == ORD001.ORDER.code && it.orderStatusCode == ORD002.ORDER_RECEIVED.code }

        val cancelableItems = orderDetails.map { detail ->
            val goods = orderGoodsRepository.findByIdOrderNoAndIdGoodsNoAndIdItemNo(orderNo, detail.goodsNo, detail.itemNo)
            CancelableItemDto(
                orderSequence = detail.id.orderSequence,
                orderProcessSequence = detail.id.orderProcessSequence,
                goodsNo = detail.goodsNo, itemNo = detail.itemNo,
                salePrice = goods?.salePrice ?: 0L, quantity = detail.quantity
            )
        }

        val payments = payBaseRepository.findByOrderNo(orderNo)
            .filter { it.payTypeCode == "001" && (it.cancelableAmount ?: 0L) > 0 }
            .map { pay ->
                PaymentInfoDto(
                    payNo = pay.payNo, payWayCode = pay.payWayCode,
                    cancelableAmount = pay.cancelableAmount ?: 0L,
                    pgTypeCode = pay.pgTypeCode, trdNo = pay.trdNo, originalAmount = pay.amount
                )
            }

        return ApiResponse.success(OrderCancelInfoResponse(orderNo, memberNo, cancelableItems, payments))
    }

    @PostMapping("/{orderNo}/cancel-details")
    @Transactional
    fun createCancelDetails(
        @PathVariable orderNo: String,
        @RequestBody request: CreateCancelDetailsRequest
    ): ApiResponse<Void> {
        val now = LocalDateTime.now()
        for (target in request.targets) {
            val original = orderDetailRepository.findByIdOrderNoAndIdOrderSequenceAndIdOrderProcessSequence(
                target.orderNo, target.orderSequence, target.orderProcessSequence
            ) ?: continue

            val maxSeq = orderDetailRepository.findByIdOrderNoAndIdOrderSequence(target.orderNo, target.orderSequence)
                .maxOfOrNull { it.id.orderProcessSequence } ?: original.id.orderProcessSequence

            val cancelDetail = OrderDetail().apply {
                this.id = OrderDetailId(orderNo = target.orderNo, orderSequence = target.orderSequence, orderProcessSequence = maxSeq + 1)
                this.upperOrderProcessSequence = target.orderProcessSequence
                this.claimNo = request.claimNo
                this.goodsNo = original.goodsNo; this.itemNo = original.itemNo
                this.quantity = original.quantity
                this.orderStatusCode = ORD002.ORDER_CANCELLED.code
                this.deliveryTypeCode = DLV001.COLLECTION.code
                this.orderTypeCode = ORD001.ORDER_CANCEL.code
                this.orderAcceptDtm = now
            }
            orderDetailTrxRepository.save(cancelDetail)
        }
        return ApiResponse.success()
    }

    @PostMapping("/{orderNo}/generate-claim-no")
    fun generateClaimNo(@PathVariable orderNo: String): ApiResponse<String> =
        ApiResponse.success(orderBaseTrxRepository.generateClaimNo())
}
