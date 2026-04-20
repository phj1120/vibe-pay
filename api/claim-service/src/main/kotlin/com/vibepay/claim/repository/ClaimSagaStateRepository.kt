package com.vibepay.claim.repository

import com.vibepay.claim.entity.ClaimSagaState
import org.springframework.data.jpa.repository.JpaRepository

interface ClaimSagaStateRepository : JpaRepository<ClaimSagaState, String>
