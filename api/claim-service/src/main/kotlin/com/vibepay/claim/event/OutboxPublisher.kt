package com.vibepay.claim.event

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
        for (msg in pending) {
            try {
                kafkaTemplate.send(msg.topic, msg.aggregateId, msg.payload).get()
                msg.status = "SENT"; msg.sentAt = LocalDateTime.now()
            } catch (e: Exception) {
                log.error("Failed to publish outbox message id={}", msg.id, e)
                msg.status = "FAILED"; msg.retryCount++
            }
            outboxRepository.save(msg)
        }
    }
}
