package com.vibepay.payment.event

import com.fasterxml.jackson.databind.ObjectMapper
import com.vibepay.messaging.EventEnvelope
import com.vibepay.payment.dto.OrderCreatedPayload
import com.vibepay.payment.service.PaymentProcessingService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class OrderEventConsumer(
    private val paymentProcessingService: PaymentProcessingService,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @KafkaListener(topics = ["order.events"], groupId = "payment-service")
    fun consume(message: String) {
        try {
            val envelope = objectMapper.readValue(message, EventEnvelope::class.java)
            when (envelope.type) {
                "OrderCreated" -> {
                    val payload = objectMapper.convertValue(envelope.payload, OrderCreatedPayload::class.java)
                    log.info("OrderCreated consumed. orderNo={}", payload.orderNo)
                    paymentProcessingService.processOrder(payload)
                }
                else -> log.debug("Unhandled order event type: {}", envelope.type)
            }
        } catch (e: Exception) {
            log.error("Failed to process order event: {}", message, e)
        }
    }
}
