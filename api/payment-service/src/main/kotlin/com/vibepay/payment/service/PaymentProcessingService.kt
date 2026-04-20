package com.vibepay.payment.service

import com.api.app.emum.MEM002
import com.api.app.emum.MEM003
import com.api.app.emum.PAY001
import com.api.app.emum.PAY002
import com.api.app.emum.PAY003
import com.api.app.emum.PAY004
import com.api.app.emum.PAY005
import com.fasterxml.jackson.databind.ObjectMapper
import com.vibepay.messaging.EventEnvelope
import com.vibepay.messaging.KafkaTopics
import com.vibepay.messaging.OutboxMessage
import com.vibepay.messaging.OutboxRepository
import com.vibepay.payment.client.CoreFeignClient
import com.vibepay.payment.dto.OrderCreatedPayload
import com.vibepay.payment.dto.PayItemPayload
import com.vibepay.payment.dto.PaymentConfirmRequest
import com.vibepay.payment.dto.PointTransactionRequest
import com.vibepay.payment.entity.PayBase
import com.vibepay.payment.entity.PayInterfaceLog
import com.vibepay.payment.repository.PayBaseRepository
import com.vibepay.payment.repository.PayInterfaceLogRepository
import com.vibepay.payment.service.strategy.PaymentGatewayFactory
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class PaymentProcessingService(
    private val payBaseRepository: PayBaseRepository,
    private val payInterfaceLogRepository: PayInterfaceLogRepository,
    private val outboxRepository: OutboxRepository,
    private val paymentGatewayFactory: PaymentGatewayFactory,
    private val coreFeignClient: CoreFeignClient,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun processOrder(payload: OrderCreatedPayload) {
        log.info("Processing payments for orderNo={}", payload.orderNo)
        val completedPayments = mutableListOf<PayBase>()
        try {
            for (item in payload.payItems) {
                val payBase = processPayItem(payload.orderNo, payload.memberNo, item)
                completedPayments.add(payBase)
            }
            publishPaymentApproved(payload.orderNo, payload.memberNo, payload.basketNos, completedPayments)
        } catch (e: Exception) {
            log.error("Payment failed for orderNo={}", payload.orderNo, e)
            rollbackPayments(completedPayments, payload.memberNo)
            publishPaymentFailed(payload.orderNo, payload.memberNo, e.message ?: "결제 실패")
        }
    }

    private fun processPayItem(orderNo: String, memberNo: String, item: PayItemPayload): PayBase {
        return when (item.payWayCode) {
            PAY002.POINT.code -> processPointPayment(orderNo, memberNo, item)
            PAY002.CREDIT_CARD.code -> processCardPayment(orderNo, memberNo, item)
            else -> throw IllegalArgumentException("지원하지 않는 결제 방식: ${item.payWayCode}")
        }
    }

    private fun processPointPayment(orderNo: String, memberNo: String, item: PayItemPayload): PayBase {
        val payBase = PayBase().apply {
            payTypeCode = PAY001.PAYMENT.code; payWayCode = PAY002.POINT.code
            payStatusCode = PAY003.PAYMENT_COMPLETED.code; this.orderNo = orderNo
            this.memberNo = memberNo; amount = item.amount; cancelableAmount = item.amount
        }
        payBaseRepository.save(payBase)
        val payNo = payBase.payNo
        val pointRequest = PointTransactionRequest(item.amount, MEM002.USE.code, MEM003.ORDER.code, payNo)
        saveLog(payNo, memberNo, PAY004.PAYMENT.code, objectMapper.writeValueAsString(pointRequest), null)
        coreFeignClient.processPointTransaction(memberNo, pointRequest)
        saveLog(payNo, memberNo, PAY004.APPROVAL.code, null, """{"payNo":"$payNo","status":"completed"}""")
        payBase.payFinishDateTime = LocalDateTime.now()
        return payBase
    }

    private fun processCardPayment(orderNo: String, memberNo: String, item: PayItemPayload): PayBase {
        val pgTypeCode = item.pgTypeCode ?: throw IllegalArgumentException("카드 결제 시 pgTypeCode 필수")
        val pgType = PAY005.findByCode(pgTypeCode) ?: throw IllegalArgumentException("지원하지 않는 PG: $pgTypeCode")
        val strategy = paymentGatewayFactory.getStrategy(pgType)

        val payBase = PayBase().apply {
            payTypeCode = PAY001.PAYMENT.code; payWayCode = PAY002.CREDIT_CARD.code
            payStatusCode = PAY003.PAYMENT_COMPLETED.code; this.orderNo = orderNo
            this.memberNo = memberNo; amount = item.amount; cancelableAmount = item.amount
            this.pgTypeCode = pgTypeCode
        }
        payBaseRepository.save(payBase)
        val payNo = payBase.payNo

        val confirmRequest = PaymentConfirmRequest(
            pgTypeCode = pgTypeCode, authToken = item.authToken, orderNo = orderNo,
            authUrl = item.authUrl, netCancelUrl = item.netCancelUrl, price = item.price,
            transactionId = item.transactionId, niceAmount = item.niceAmount,
            tradeNo = item.tradeNo, mid = item.mid
        )
        saveLog(payNo, memberNo, PAY004.PAYMENT.code, objectMapper.writeValueAsString(confirmRequest), null)
        val result = strategy.approvePayment(confirmRequest)
        saveLog(payNo, memberNo, PAY004.APPROVAL.code, null, objectMapper.writeValueAsString(result))

        payBase.approveNo = result.approveNo; payBase.trdNo = result.trdNo
        payBase.payFinishDateTime = LocalDateTime.now()
        return payBase
    }

    private fun publishPaymentApproved(orderNo: String, memberNo: String, basketNos: List<String>, payments: List<PayBase>) {
        val earnPointAmount = payments.filter { it.payWayCode == PAY002.CREDIT_CARD.code }.sumOf { it.amount } / 10
        val payload = mapOf("orderNo" to orderNo, "memberNo" to memberNo, "basketNos" to basketNos, "earnPointAmount" to earnPointAmount)
        saveOutbox(orderNo, "PaymentApproved", KafkaTopics.PAYMENT_EVENTS, payload)
        log.info("PaymentApproved outbox saved. orderNo={}", orderNo)
    }

    private fun publishPaymentFailed(orderNo: String, memberNo: String, reason: String) {
        val payload = mapOf("orderNo" to orderNo, "memberNo" to memberNo, "reason" to reason)
        saveOutbox(orderNo, "PaymentFailed", KafkaTopics.PAYMENT_EVENTS, payload)
        log.info("PaymentFailed outbox saved. orderNo={}", orderNo)
    }

    private fun rollbackPayments(payments: List<PayBase>, memberNo: String) {
        for (p in payments.reversed()) {
            try {
                if (p.payWayCode == PAY002.POINT.code) {
                    val refundRequest = PointTransactionRequest(p.amount, MEM002.EARN.code, MEM003.CANCEL.code, p.payNo)
                    coreFeignClient.processPointTransaction(p.memberNo, refundRequest)
                }
                // Card: PG 망취소 (TODO)
            } catch (e: Exception) {
                log.error("Rollback failed for payNo={}", p.payNo, e)
            }
        }
    }

    private fun saveLog(payNo: String, memberNo: String, code: String, req: String?, res: String?) {
        try {
            payInterfaceLogRepository.save(PayInterfaceLog().apply {
                this.payNo = payNo; this.memberNo = memberNo; payLogCode = code
                requestJson = req; responseJson = res
            })
        } catch (e: Exception) {
            log.error("Failed to save interface log. payNo={}", payNo, e)
        }
    }

    private fun saveOutbox(orderNo: String, eventType: String, topic: String, payload: Map<String, Any>) {
        val envelope = EventEnvelope(type = eventType, aggregateType = "Payment", aggregateId = orderNo, payload = payload)
        outboxRepository.save(OutboxMessage(aggregateType = "Payment", aggregateId = orderNo, eventType = eventType, topic = topic, payload = objectMapper.writeValueAsString(envelope)))
    }

    @Transactional
    fun cancelByOrder(orderNo: String, claimNo: String, memberNo: String, cancelAmounts: Map<String, Long>) {
        val payments = payBaseRepository.findByOrderNo(orderNo).filter { cancelAmounts.containsKey(it.payNo) }
        for (payment in payments) {
            val cancelAmount = cancelAmounts[payment.payNo]!!
            val cancelPayment = PayBase().apply {
                payTypeCode = PAY001.REFUND.code; payWayCode = payment.payWayCode
                payStatusCode = PAY003.PAYMENT_CANCELLED.code; this.orderNo = payment.orderNo
                this.claimNo = claimNo; upperPayNo = payment.payNo
                payFinishDateTime = LocalDateTime.now(); this.memberNo = memberNo
                amount = cancelAmount; cancelableAmount = 0L; pgTypeCode = payment.pgTypeCode
                trdNo = payment.trdNo
            }
            payBaseRepository.save(cancelPayment)

            when (payment.payWayCode) {
                PAY002.CREDIT_CARD.code -> {
                    val pgType = PAY005.findByCode(payment.pgTypeCode!!)!!
                    paymentGatewayFactory.getStrategy(pgType).cancelPayment(
                        cancelPayment.payNo, orderNo, payment.trdNo!!, cancelAmount,
                        payment.cancelableAmount, payment.amount, "주문 취소"
                    )
                }
                PAY002.POINT.code -> {
                    val refundRequest = PointTransactionRequest(cancelAmount, MEM002.EARN.code, MEM003.CANCEL.code, cancelPayment.payNo)
                    coreFeignClient.processPointTransaction(memberNo, refundRequest)
                }
            }
            payBaseRepository.updateCancelableAmount(payment.payNo, cancelAmount)
        }
    }

    fun getPaymentsByOrderNo(orderNo: String): List<PayBase> = payBaseRepository.findByOrderNo(orderNo)
}
