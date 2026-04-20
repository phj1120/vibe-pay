package com.api.app.messaging

import com.vibepay.messaging.OutboxMessage
import com.vibepay.messaging.OutboxRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class OutboxPublisher(
    private val outboxRepository: OutboxRepository,
    private val kafkaTemplate: KafkaTemplate<String, String>
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    @Scheduled(fixedDelay = 500)
    @Transactional
    fun publishPendingMessages() {
        val pending = outboxRepository.findPendingMessages(PageRequest.of(0, 100))
        if (pending.isEmpty()) return

        pending.forEach { message ->
            try {
                kafkaTemplate.send(message.topic, message.aggregateId, message.payload)
                message.status = "SENT"
                message.sentAt = LocalDateTime.now()
                outboxRepository.save(message)
            } catch (e: Exception) {
                log.error("Failed to publish outbox message id={}: {}", message.id, e.message)
                message.retryCount++
                if (message.retryCount >= 5) {
                    message.status = "FAILED"
                    log.error("Outbox message permanently failed id={}", message.id)
                }
                outboxRepository.save(message)
            }
        }
    }
}
