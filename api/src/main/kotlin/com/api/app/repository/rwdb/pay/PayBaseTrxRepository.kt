package com.api.app.repository.rwdb.pay

import com.api.app.entity.PayBase
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface PayBaseTrxRepository : JpaRepository<PayBase, String> {

    @Query(value = "SELECT LPAD(NEXTVAL('SEQ_PAY_NO')::TEXT, 15, '0')", nativeQuery = true)
    fun generatePayNo(): String

    @Modifying
    @Query(
        value = "UPDATE PAY_BASE SET CANCELABLE_AMOUNT = CANCELABLE_AMOUNT - :cancelAmount, MODIFY_DATE_TIME = CURRENT_TIMESTAMP WHERE PAY_NO = :payNo",
        nativeQuery = true
    )
    fun updateCancelableAmount(payNo: String, cancelAmount: Long): Int
}
