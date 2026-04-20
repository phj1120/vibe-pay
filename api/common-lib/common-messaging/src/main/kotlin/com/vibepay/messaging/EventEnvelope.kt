package com.vibepay.messaging

import java.time.LocalDateTime
import java.util.UUID

data class EventEnvelope(
    val eventId: String = UUID.randomUUID().toString(),
    val occurredAt: LocalDateTime = LocalDateTime.now(),
    val type: String,
    val schemaVersion: Int = 1,
    val aggregateType: String,
    val aggregateId: String,
    val payload: Map<String, Any?>
)
