package com.api.app.repository.rodb.pay

import com.api.app.entity.PayBase
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface OrderCompletePaymentProjection {
    fun getPayWayCode(): String?
    fun getPayWayName(): String?
    fun getAmount(): Long?
    fun getPgTypeCode(): String?
    fun getPgTypeName(): String?
}

interface PayBaseRepository : JpaRepository<PayBase, String> {

    fun findByOrderNo(orderNo: String): List<PayBase>
    fun findByMemberNo(memberNo: String): List<PayBase>
    fun findByApproveNo(approveNo: String): PayBase?

    @Query(
        value = """
        SELECT PAY_WAY_CODE AS payWayCode
             , CASE PAY_WAY_CODE WHEN '001' THEN '신용카드' WHEN '002' THEN '포인트' ELSE '' END AS payWayName
             , AMOUNT AS amount
             , PG_TYPE_CODE AS pgTypeCode
             , CASE PG_TYPE_CODE WHEN '001' THEN '이니시스' WHEN '002' THEN '나이스' WHEN '999' THEN '테스트PG' ELSE '' END AS pgTypeName
          FROM PAY_BASE
         WHERE ORDER_NO = :orderNo
           AND PAY_TYPE_CODE = '001'
           AND CLAIM_NO IS NULL
         ORDER BY PAY_NO
        """,
        nativeQuery = true
    )
    fun selectOrderCompletePaymentByOrderNo(orderNo: String): List<OrderCompletePaymentProjection>
}
