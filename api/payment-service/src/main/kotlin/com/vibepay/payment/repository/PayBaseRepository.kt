package com.vibepay.payment.repository

import com.vibepay.payment.entity.PayBase
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface PayBaseRepository : JpaRepository<PayBase, String> {

    fun findByOrderNo(orderNo: String): List<PayBase>

    @Modifying
    @Query(
        value = "UPDATE PAY_BASE SET CANCELABLE_AMOUNT = CANCELABLE_AMOUNT - :cancelAmount, MODIFY_DATE_TIME = CURRENT_TIMESTAMP WHERE PAY_NO = :payNo",
        nativeQuery = true
    )
    fun updateCancelableAmount(payNo: String, cancelAmount: Long): Int
}
