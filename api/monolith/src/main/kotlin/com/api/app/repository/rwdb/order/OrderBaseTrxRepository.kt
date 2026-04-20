package com.api.app.repository.rwdb.order

import com.api.app.entity.OrderBase
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface OrderBaseTrxRepository : JpaRepository<OrderBase, String> {

    @Query(
        value = "SELECT TO_CHAR(CURRENT_DATE, 'YYYYMMDD') || 'O' || LPAD(NEXTVAL('SEQ_ORDER_NO')::TEXT, 6, '0')",
        nativeQuery = true
    )
    fun generateOrderNo(): String

    @Query(
        value = "SELECT TO_CHAR(CURRENT_DATE, 'YYYYMMDD') || 'C' || LPAD(NEXTVAL('SEQ_CLAIM_NO')::TEXT, 6, '0')",
        nativeQuery = true
    )
    fun generateClaimNo(): String

    @Modifying
    @Query("UPDATE OrderBase o SET o.orderStatusCode = :status WHERE o.orderNo = :orderNo")
    fun updateOrderStatus(orderNo: String, status: String): Int
}
