package com.vibepay.payment.repository

import com.vibepay.payment.entity.PayInterfaceLog
import org.springframework.data.jpa.repository.JpaRepository

interface PayInterfaceLogRepository : JpaRepository<PayInterfaceLog, String>
