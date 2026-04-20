package com.vibepay.messaging

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface OutboxRepository : JpaRepository<OutboxMessage, Long> {

    @Query("SELECT o FROM OutboxMessage o WHERE o.status = 'PENDING' ORDER BY o.id ASC")
    fun findPendingMessages(pageable: Pageable): List<OutboxMessage>
}
