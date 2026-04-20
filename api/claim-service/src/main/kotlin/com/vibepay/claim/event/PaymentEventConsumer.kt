package com.vibepay.claim.event

import com.fasterxml.jackson.databind.ObjectMapper
import com.vibepay.claim.service.ClaimService
import com.vibepay.messaging.EventEnvelope
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class PaymentEventConsumer(
    private val claimService: ClaimService,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @KafkaListener(topics = ["payment.events"], groupId = "claim-service")
    fun consume(message: String) {
        try {
            val envelope = objectMapper.readValue(message, EventEnvelope::class.java)
            val payload = envelope.payload as Map<*, *>
            val claimNo = payload["claimNo"] as? String ?: return

            when (envelope.type) {
                "ClaimPaymentCancelled" -> {
                    val orderNo = payload["orderNo"] as? String ?: ""
                    val memberNo = payload["memberNo"] as? String ?: ""
                    @Suppress("UNCHECKED_CAST")
                    val targets = (payload["targets"] as? List<*>)?.filterIsInstance<Map<String, Any>>() ?: emptyList()
                    log.info("ClaimPaymentCancelled consumed. claimNo={}", claimNo)
                    claimService.handlePaymentCancelled(claimNo, orderNo, memberNo, targets)
                }
                "ClaimPaymentCancelFailed" -> {
                    val reason = payload["reason"] as? String ?: "unknown"
                    log.warn("ClaimPaymentCancelFailed consumed. claimNo={}", claimNo)
                    claimService.handlePaymentCancelFailed(claimNo, reason)
                }
                else -> {}
            }
        } catch (e: Exception) {
            log.error("Failed to process payment event in claim-service: {}", message, e)
        }
    }
}
