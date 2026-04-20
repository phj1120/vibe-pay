package com.vibepay.core.event

import com.fasterxml.jackson.databind.ObjectMapper
import com.vibepay.core.service.BasketService
import com.vibepay.core.service.PointService
import com.vibepay.core.dto.request.point.PointTransactionRequest
import com.vibepay.messaging.EventEnvelope
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class OrderEventConsumer(
    private val basketService: BasketService,
    private val pointService: PointService,
    private val objectMapper: ObjectMapper
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    @KafkaListener(topics = ["order.events"], groupId = "core-service")
    fun consume(message: String) {
        val envelope = try {
            objectMapper.readValue(message, EventEnvelope::class.java)
        } catch (e: Exception) {
            log.error("Failed to parse order event: {}", message, e)
            return
        }

        when (envelope.type) {
            "OrderConfirmed" -> handleOrderConfirmed(envelope)
            else -> log.debug("Ignoring order event type: {}", envelope.type)
        }
    }

    private fun handleOrderConfirmed(envelope: EventEnvelope) {
        try {
            val payload = envelope.payload
            val memberNo = payload["memberNo"] as? String ?: return
            val basketNos = (payload["basketNos"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()

            basketNos.forEach { basketNo ->
                basketService.markBasketAsOrdered(basketNo)
            }

            val earnAmount = (payload["earnPointAmount"] as? Number)?.toLong() ?: 0L
            if (earnAmount > 0) {
                val orderNo = payload["orderNo"] as? String ?: envelope.aggregateId
                pointService.processPointTransaction(
                    memberNo,
                    PointTransactionRequest(
                        amount = earnAmount,
                        pointTransactionCode = "001",
                        pointTransactionReasonCode = "002",
                        pointTransactionReasonNo = orderNo
                    )
                )
            }
            log.info("OrderConfirmed processed: orderNo={}, baskets={}", envelope.aggregateId, basketNos.size)
        } catch (e: Exception) {
            log.error("Failed to handle OrderConfirmed: eventId={}", envelope.eventId, e)
        }
    }
}
