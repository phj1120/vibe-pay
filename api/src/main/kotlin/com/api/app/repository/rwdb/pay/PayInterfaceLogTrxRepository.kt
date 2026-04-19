package com.api.app.repository.rwdb.pay

import com.api.app.entity.PayInterfaceLog
import org.springframework.data.jpa.repository.JpaRepository

interface PayInterfaceLogTrxRepository : JpaRepository<PayInterfaceLog, String>
