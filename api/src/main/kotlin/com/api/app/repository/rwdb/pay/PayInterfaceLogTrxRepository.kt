package com.api.app.repository.rwdb.pay

import com.api.app.entity.PayInterfaceLog
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PayInterfaceLogTrxRepository : JpaRepository<PayInterfaceLog, String> {

    @Query(value = "SELECT LPAD(NEXTVAL('SEQ_PAY_INTERFACE_NO')::TEXT, 15, '0')", nativeQuery = true)
    fun generatePayInterfaceNo(): String
}
