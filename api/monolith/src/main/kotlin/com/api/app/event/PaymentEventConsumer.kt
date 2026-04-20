package com.api.app.event

import com.api.app.service.order.OrderServiceImpl
import com.fasterxml.jackson.databind.ObjectMapper
import com.vibepay.messaging.EventEnvelope
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class PaymentEventConsumer(
    private val orderServiceImpl: OrderServiceImpl,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @KafkaListener(topics = ["payment.events"], groupId = "order-service")
    fun consume(message: String) {
        try {
            val envelope = objectMapper.readValue(message, EventEnvelope::class.java)
            val payload = envelope.payload as Map<*, *>
            val orderNo = payload["orderNo"] as? String ?: return

            when (envelope.type) {
                "PaymentApproved" -> {
                    val memberNo = payload["memberNo"] as? String ?: ""
                    @Suppress("UNCHECKED_CAST")
                    val basketNos = (payload["basketNos"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                    val earnPointAmount = when (val v = payload["earnPointAmount"]) {
                        is Number -> v.toLong()
                        else -> 0L
                    }
                    log.info("PaymentApproved consumed. orderNo={}", orderNo)
                    orderServiceImpl.handlePaymentApproved(orderNo, memberNo, basketNos, earnPointAmount)
                }
                "PaymentFailed" -> {
                    val memberNo = payload["memberNo"] as? String ?: ""
                    val reason = payload["reason"] as? String ?: "unknown"
                    log.warn("PaymentFailed consumed. orderNo={}, reason={}", orderNo, reason)
                    orderServiceImpl.handlePaymentFailed(orderNo, memberNo, reason)
                }
                else -> log.debug("Unhandled payment event: {}", envelope.type)
            }
        } catch (e: Exception) {
            log.error("Failed to process payment event: {}", message, e)
        }
    }
}
