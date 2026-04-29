package com.api.app.service.claim

import com.api.app.dto.request.claim.CancelRequest
import com.api.app.dto.request.claim.ClaimTargetRequest
import com.api.app.dto.request.payment.PaymentCancelRequest
import com.api.app.dto.request.point.PointTransactionRequest
import com.api.app.emum.DLV001
import com.api.app.emum.MEM002
import com.api.app.emum.MEM003
import com.api.app.emum.ORD001
import com.api.app.emum.ORD002
import com.api.app.emum.PAY001
import com.api.app.emum.PAY002
import com.api.app.emum.PAY003
import com.api.app.emum.PAY004
import com.api.app.emum.PAY005
import com.api.app.entity.OrderDetail
import com.api.app.entity.OrderDetailId
import com.api.app.entity.PayBase
import com.api.app.entity.PayInterfaceLog
import com.api.app.repository.rodb.order.OrderBaseRepository
import com.api.app.repository.rodb.order.OrderDetailRepository
import com.api.app.repository.rodb.order.OrderGoodsRepository
import com.api.app.repository.rodb.pay.PayBaseRepository
import com.api.app.repository.rwdb.order.OrderBaseTrxRepository
import com.api.app.repository.rwdb.order.OrderDetailTrxRepository
import com.api.app.repository.rwdb.pay.PayBaseTrxRepository
import com.api.app.repository.rwdb.pay.PayInterfaceLogTrxRepository
import com.api.app.service.payment.strategy.PaymentGatewayFactory
import com.api.app.service.point.PointService
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class ClaimServiceImpl(
    private val orderBaseTrxRepository: OrderBaseTrxRepository,
    private val orderBaseRepository: OrderBaseRepository,
    private val orderDetailRepository: OrderDetailRepository,
    private val orderDetailTrxRepository: OrderDetailTrxRepository,
    private val orderGoodsRepository: OrderGoodsRepository,
    private val payBaseRepository: PayBaseRepository,
    private val payBaseTrxRepository: PayBaseTrxRepository,
    private val payInterfaceLogTrxRepository: PayInterfaceLogTrxRepository,
    private val pointService: PointService,
    private val paymentGatewayFactory: PaymentGatewayFactory,
    private val objectMapper: ObjectMapper
) : ClaimService {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun cancelOrder(request: CancelRequest) {
        log.info("Order cancel started. memberNo={}, targetCount={}", request.memberNo, request.targets.size)

        validateCancelRequest(request)

        val claimNo = orderBaseTrxRepository.generateClaimNo()
        log.info("Claim number generated: {}", claimNo)

        val targetsByOrderNo = request.targets.groupBy { it.orderNo }

        for ((orderNo, targets) in targetsByOrderNo) {
            log.info("Processing cancel for orderNo={}, claimNo={}, targetCount={}", orderNo, claimNo, targets.size)

            validateCancelTargets(targets, request.memberNo)
            val cancelAmounts = calculateCancelAmounts(orderNo, targets)
            processCancelPayments(orderNo, claimNo, cancelAmounts, request.memberNo)
            createCancelOrderDetails(orderNo, claimNo, targets)
        }

        log.info("Order cancel completed successfully. memberNo={}", request.memberNo)
    }

    private fun validateCancelRequest(request: CancelRequest) {
        if (request.targets.isEmpty()) {
            throw IllegalArgumentException("취소 대상이 없습니다")
        }
        if (request.targets.distinct().size != request.targets.size) {
            throw IllegalArgumentException("중복된 취소 대상이 포함되어 있습니다")
        }
    }

    private fun validateCancelTargets(targets: List<ClaimTargetRequest>, memberNo: String) {
        for (target in targets) {
            val orderBase = orderBaseRepository.findById(target.orderNo).orElse(null)
                ?: throw IllegalArgumentException("주문 정보를 찾을 수 없습니다")
            if (orderBase.memberNo != memberNo) {
                throw IllegalArgumentException("본인 주문만 취소할 수 있습니다")
            }

            val orderDetail = orderDetailRepository.findByIdOrderNoAndIdOrderSequenceAndIdOrderProcessSequence(
                target.orderNo, target.orderSequence, target.orderProcessSequence
            ) ?: throw IllegalArgumentException("주문 정보를 찾을 수 없습니다")

            if (orderDetail.orderTypeCode != ORD001.ORDER.code) {
                throw IllegalArgumentException("주문 건만 취소할 수 있습니다")
            }
            if (orderDetail.orderStatusCode != ORD002.ORDER_RECEIVED.code) {
                throw IllegalArgumentException("주문접수 상태의 주문만 취소할 수 있습니다")
            }
        }
    }

    private fun calculateCancelAmounts(orderNo: String, targets: List<ClaimTargetRequest>): Map<String, Long> {
        var totalCancelAmount = 0L

        for (target in targets) {
            val orderDetail = orderDetailRepository.findByIdOrderNoAndIdOrderSequenceAndIdOrderProcessSequence(
                target.orderNo, target.orderSequence, target.orderProcessSequence
            )!!
            val orderGoods = orderGoodsRepository.findByIdOrderNoAndIdGoodsNoAndIdItemNo(
                target.orderNo, orderDetail.goodsNo, orderDetail.itemNo
            )!!
            totalCancelAmount += orderGoods.salePrice * orderDetail.quantity
        }

        val originalPayments = payBaseRepository.findByOrderNo(orderNo)
            .filter { it.payTypeCode == "001" }
            .sortedBy { PAY002.findByCode(it.payWayCode)?.displaySequence ?: Int.MAX_VALUE }

        val cancelAmounts = mutableMapOf<String, Long>()
        var remainingAmount = totalCancelAmount

        for (payment in originalPayments) {
            if (remainingAmount <= 0) break
            val cancelableAmount = payment.cancelableAmount ?: 0L
            if (cancelableAmount <= 0) continue

            val cancelAmount = minOf(remainingAmount, cancelableAmount)
            cancelAmounts[payment.payNo] = cancelAmount
            remainingAmount -= cancelAmount

            log.info("Cancel amount calculated. payNo={}, payWayCode={}, cancelAmount={}", payment.payNo, payment.payWayCode, cancelAmount)
        }

        if (remainingAmount > 0) throw IllegalStateException("취소 가능한 금액이 부족합니다")
        return cancelAmounts
    }

    private fun processCancelPayments(orderNo: String, claimNo: String,
                                       cancelAmounts: Map<String, Long>, memberNo: String) {
        val now = LocalDateTime.now()

        val paymentsToCancel = payBaseRepository.findByOrderNo(orderNo)
            .filter { cancelAmounts.containsKey(it.payNo) }
            .sortedByDescending { PAY002.findByCode(it.payWayCode)?.displaySequence ?: 0 }

        for (originalPayment in paymentsToCancel) {
            val cancelAmount = cancelAmounts[originalPayment.payNo]!!

            when (originalPayment.payWayCode) {
                PAY002.CREDIT_CARD.code -> processCreditCardCancel(originalPayment, cancelAmount, claimNo, memberNo, now)
                PAY002.POINT.code -> processPointCancel(originalPayment, cancelAmount, claimNo, memberNo, now)
            }

            payBaseTrxRepository.updateCancelableAmount(originalPayment.payNo, cancelAmount)
            log.info("Payment cancelled successfully. payNo={}, payWayCode={}, cancelAmount={}", originalPayment.payNo, originalPayment.payWayCode, cancelAmount)
        }
    }

    private fun processCreditCardCancel(originalPayment: PayBase, cancelAmount: Long,
                                         claimNo: String, memberNo: String, now: LocalDateTime) {
        val cancelPayment = PayBase().apply {
            this.payTypeCode = PAY001.REFUND.code
            this.payWayCode = originalPayment.payWayCode
            this.payStatusCode = PAY003.PAYMENT_CANCELLED.code
            this.orderNo = originalPayment.orderNo
            this.claimNo = claimNo
            this.upperPayNo = originalPayment.payNo
            this.payFinishDateTime = now
            this.memberNo = memberNo
            this.amount = cancelAmount
            this.cancelableAmount = 0L
            this.pgTypeCode = originalPayment.pgTypeCode
            this.trdNo = originalPayment.trdNo
        }
        payBaseTrxRepository.save(cancelPayment)
        val payNo = cancelPayment.payNo

        val pgType = PAY005.findByCode(originalPayment.pgTypeCode!!)!!
        val strategy = paymentGatewayFactory.getStrategy(pgType)

        val partialCancelCode = if (originalPayment.cancelableAmount == cancelAmount) "0" else "1"

        val cancelRequest = PaymentCancelRequest(
            pgTypeCode = originalPayment.pgTypeCode!!,
            transactionId = originalPayment.trdNo!!,
            orderNo = originalPayment.orderNo!!,
            cancelAmount = cancelAmount,
            cancelReason = "주문 취소",
            partialCancelCode = partialCancelCode,
            originalAmount = originalPayment.amount,
            cancelableAmount = originalPayment.cancelableAmount
        )

        val requestJson = objectMapper.writeValueAsString(cancelRequest)
        var responseJson: String? = null

        try {
            strategy.cancelPaymentByOrder(cancelRequest)
            responseJson = """{"success": true}"""
        } catch (e: Exception) {
            responseJson = """{"success": false, "error": "${e.message}"}"""
            throw e
        } finally {
            saveInterfaceLog(payNo, memberNo, PAY004.CANCEL.code, requestJson, responseJson)
        }

        log.info("Credit card cancel payment created. payNo={}, cancelAmount={}", payNo, cancelAmount)
    }

    private fun processPointCancel(originalPayment: PayBase, cancelAmount: Long,
                                    claimNo: String, memberNo: String, now: LocalDateTime) {
        val cancelPayment = PayBase().apply {
            this.payTypeCode = PAY001.REFUND.code
            this.payWayCode = originalPayment.payWayCode
            this.payStatusCode = PAY003.PAYMENT_CANCELLED.code
            this.orderNo = originalPayment.orderNo
            this.claimNo = claimNo
            this.upperPayNo = originalPayment.payNo
            this.payFinishDateTime = now
            this.memberNo = memberNo
            this.amount = cancelAmount
            this.cancelableAmount = 0L
        }
        payBaseTrxRepository.save(cancelPayment)
        val payNo = cancelPayment.payNo

        val pointRequest = PointTransactionRequest(
            amount = cancelAmount,
            pointTransactionCode = MEM002.EARN.code,
            pointTransactionReasonCode = MEM003.CANCEL.code,
            pointTransactionReasonNo = payNo
        )

        pointService.processPointTransaction(memberNo, pointRequest)
        log.info("Point cancel payment created. payNo={}, cancelAmount={}", payNo, cancelAmount)
    }

    private fun createCancelOrderDetails(orderNo: String, claimNo: String, targets: List<ClaimTargetRequest>) {
        val now = LocalDateTime.now()

        for (target in targets) {
            val originalDetail = orderDetailRepository.findByIdOrderNoAndIdOrderSequenceAndIdOrderProcessSequence(
                target.orderNo, target.orderSequence, target.orderProcessSequence
            )!!

            val cancelDetail = OrderDetail().apply {
                this.id = OrderDetailId(
                    orderNo = orderNo,
                    orderSequence = target.orderSequence,
                    orderProcessSequence = originalDetail.orderProcessSequence + 1
                )
                this.upperOrderProcessSequence = target.orderProcessSequence
                this.claimNo = claimNo
                this.goodsNo = originalDetail.goodsNo
                this.itemNo = originalDetail.itemNo
                this.quantity = originalDetail.quantity
                this.orderStatusCode = ORD002.ORDER_CANCELLED.code
                this.deliveryTypeCode = DLV001.COLLECTION.code
                this.orderTypeCode = ORD001.ORDER_CANCEL.code
                this.orderAcceptDtm = now
            }
            orderDetailTrxRepository.save(cancelDetail)

            log.info("Cancel order detail created. orderNo={}, orderSequence={}, orderProcessSequence={}",
                orderNo, cancelDetail.id.orderSequence, cancelDetail.id.orderProcessSequence)
        }
    }

    private fun saveInterfaceLog(payNo: String, memberNo: String, payLogCode: String,
                                  requestJson: String?, responseJson: String?) {
        try {
            val interfaceLog = PayInterfaceLog().apply {
                this.memberNo = memberNo
                this.payNo = payNo
                this.payLogCode = payLogCode
                this.requestJson = requestJson
                this.responseJson = responseJson
            }
            payInterfaceLogTrxRepository.save(interfaceLog)
        } catch (e: Exception) {
            log.error("Error creating pay_interface_log. payNo={}", payNo, e)
        }
    }
}
