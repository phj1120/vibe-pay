package com.vibepay.claim.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.vibepay.claim.client.OrderFeignClient
import com.vibepay.claim.dto.CancelRequest
import com.vibepay.claim.dto.CancelableItem
import com.vibepay.claim.dto.ClaimTargetRequest
import com.vibepay.claim.dto.CancelTargetDto
import com.vibepay.claim.dto.CreateCancelDetailsRequest
import com.vibepay.claim.entity.ClaimBase
import com.vibepay.claim.entity.ClaimSagaState
import com.vibepay.claim.repository.ClaimBaseRepository
import com.vibepay.claim.repository.ClaimSagaStateRepository
import com.vibepay.messaging.EventEnvelope
import com.vibepay.messaging.KafkaTopics
import com.vibepay.messaging.OutboxMessage
import com.vibepay.messaging.OutboxRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ClaimService(
    private val claimBaseRepository: ClaimBaseRepository,
    private val claimSagaStateRepository: ClaimSagaStateRepository,
    private val outboxRepository: OutboxRepository,
    private val orderFeignClient: OrderFeignClient,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun cancelOrder(request: CancelRequest) {
        log.info("Claim cancel started. memberNo={}, targets={}", request.memberNo, request.targets.size)

        val targetsByOrderNo = request.targets.groupBy { it.orderNo }
        for ((orderNo, targets) in targetsByOrderNo) {
            val cancelInfo = orderFeignClient.getOrderCancelInfo(orderNo, request.memberNo).data
                ?: throw IllegalStateException("주문 취소 정보 조회 실패")

            val claimNo = orderFeignClient.generateClaimNo(orderNo).data
                ?: throw IllegalStateException("클레임 번호 생성 실패")

            val cancelAmounts = calculateCancelAmounts(cancelInfo.cancelableItems, targets, cancelInfo.payments)

            val claim = ClaimBase().apply {
                this.claimNo = claimNo; this.orderNo = orderNo
                this.memberNo = request.memberNo; claimStatusCode = "REQUESTED"
                claimReason = request.claimReason
            }
            claimBaseRepository.save(claim)
            claimSagaStateRepository.save(ClaimSagaState().apply { this.claimNo = claimNo })

            val payload = mapOf(
                "claimNo" to claimNo, "orderNo" to orderNo, "memberNo" to request.memberNo,
                "cancelAmounts" to cancelAmounts,
                "targets" to targets.map { mapOf("orderNo" to it.orderNo, "orderSequence" to it.orderSequence, "orderProcessSequence" to it.orderProcessSequence) }
            )
            saveOutbox(claimNo, "ClaimRequested", KafkaTopics.CLAIM_EVENTS, payload)
            log.info("ClaimRequested outbox saved. claimNo={}, orderNo={}", claimNo, orderNo)
        }
    }

    @Transactional
    fun handlePaymentCancelled(claimNo: String, orderNo: String, memberNo: String, targets: List<Map<String, Any>>) {
        val saga = claimSagaStateRepository.findById(claimNo).orElse(null) ?: return
        saga.paymentDone = true
        claimSagaStateRepository.save(saga)

        val claimTargets = targets.map {
            CancelTargetDto(
                orderNo = it["orderNo"] as String,
                orderSequence = (it["orderSequence"] as Number).toLong(),
                orderProcessSequence = (it["orderProcessSequence"] as Number).toLong()
            )
        }
        orderFeignClient.createCancelDetails(orderNo, CreateCancelDetailsRequest(claimNo, memberNo, claimTargets))
        claimBaseRepository.updateStatus(claimNo, "COMPLETED")

        val claim = claimBaseRepository.findById(claimNo).orElse(null)
        claim?.completeDateTime = java.time.LocalDateTime.now()
        log.info("Claim COMPLETED. claimNo={}", claimNo)
    }

    @Transactional
    fun handlePaymentCancelFailed(claimNo: String, reason: String) {
        claimBaseRepository.updateStatus(claimNo, "FAILED")
        log.warn("Claim FAILED. claimNo={}, reason={}", claimNo, reason)
    }

    private fun calculateCancelAmounts(
        items: List<CancelableItem>,
        targets: List<ClaimTargetRequest>,
        payments: List<com.vibepay.claim.dto.PaymentInfo>
    ): Map<String, Long> {
        val targetSet = targets.map { "${it.orderNo}-${it.orderSequence}" }.toSet()
        val totalCancel = items
            .filter { "${it.goodsNo}-${it.quantity}" in targetSet || true }
            .filter { item -> targets.any { t -> t.orderSequence == item.orderSequence } }
            .sumOf { it.salePrice * it.quantity }

        val sortedPayments = payments.sortedByDescending { it.cancelableAmount }
        val cancelAmounts = mutableMapOf<String, Long>()
        var remaining = totalCancel
        for (p in sortedPayments) {
            if (remaining <= 0) break
            val cancel = minOf(remaining, p.cancelableAmount)
            if (cancel > 0) { cancelAmounts[p.payNo] = cancel; remaining -= cancel }
        }
        return cancelAmounts
    }

    private fun saveOutbox(aggregateId: String, eventType: String, topic: String, payload: Map<String, Any?>) {
        val envelope = EventEnvelope(type = eventType, aggregateType = "Claim", aggregateId = aggregateId, payload = payload)
        outboxRepository.save(OutboxMessage(aggregateType = "Claim", aggregateId = aggregateId, eventType = eventType, topic = topic, payload = objectMapper.writeValueAsString(envelope)))
    }
}
