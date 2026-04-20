package com.vibepay.payment.event

import com.fasterxml.jackson.databind.ObjectMapper
import com.vibepay.messaging.EventEnvelope
import com.vibepay.messaging.KafkaTopics
import com.vibepay.messaging.OutboxMessage
import com.vibepay.messaging.OutboxRepository
import com.vibepay.payment.service.PaymentProcessingService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ClaimEventConsumer(
    private val paymentProcessingService: PaymentProcessingService,
    private val outboxRepository: OutboxRepository,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @KafkaListener(topics = ["claim.events"], groupId = "payment-service")
    @Transactional
    fun consume(message: String) {
        try {
            val envelope = objectMapper.readValue(message, EventEnvelope::class.java)
            if (envelope.type != "ClaimRequested") return

            val payload = envelope.payload as Map<*, *>
            val claimNo = payload["claimNo"] as? String ?: return
            val orderNo = payload["orderNo"] as? String ?: return
            val memberNo = payload["memberNo"] as? String ?: return
            @Suppress("UNCHECKED_CAST")
            val cancelAmounts = (payload["cancelAmounts"] as? Map<*, *>)
                ?.entries?.associate { (k, v) -> k.toString() to (v as Number).toLong() } ?: emptyMap()
            @Suppress("UNCHECKED_CAST")
            val targets = (payload["targets"] as? List<*>)?.filterIsInstance<Map<String, Any>>() ?: emptyList()

            log.info("ClaimRequested consumed. claimNo={}, orderNo={}", claimNo, orderNo)

            try {
                paymentProcessingService.cancelByOrder(orderNo, claimNo, memberNo, cancelAmounts)
                saveOutbox(claimNo, "ClaimPaymentCancelled", KafkaTopics.PAYMENT_EVENTS,
                    mapOf("claimNo" to claimNo, "orderNo" to orderNo, "memberNo" to memberNo, "targets" to targets))
                log.info("ClaimPaymentCancelled outbox saved. claimNo={}", claimNo)
            } catch (e: Exception) {
                log.error("Payment cancellation failed for claimNo={}", claimNo, e)
                saveOutbox(claimNo, "ClaimPaymentCancelFailed", KafkaTopics.PAYMENT_EVENTS,
                    mapOf("claimNo" to claimNo, "orderNo" to orderNo, "reason" to (e.message ?: "취소 실패")))
            }
        } catch (e: Exception) {
            log.error("Failed to process claim event: {}", message, e)
        }
    }

    private fun saveOutbox(aggregateId: String, eventType: String, topic: String, payload: Map<String, Any?>) {
        val envelope = EventEnvelope(type = eventType, aggregateType = "Payment", aggregateId = aggregateId, payload = payload)
        outboxRepository.save(OutboxMessage(aggregateType = "Payment", aggregateId = aggregateId, eventType = eventType, topic = topic, payload = objectMapper.writeValueAsString(envelope)))
    }
}
