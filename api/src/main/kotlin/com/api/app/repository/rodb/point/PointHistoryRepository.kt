package com.api.app.repository.rodb.point

import com.api.app.entity.PointHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface PointHistoryProjection {
    fun getPointHistoryNo(): String?
    fun getAmount(): Long?
    fun getPointTransactionCode(): String?
    fun getPointTransactionReasonCode(): String?
    fun getPointTransactionReasonNo(): String?
    fun getStartDateTime(): LocalDateTime?
    fun getEndDateTime(): LocalDateTime?
    fun getRemainPoint(): Long?
    fun getCreatedDate(): LocalDateTime?
}

interface PointHistoryRepository : JpaRepository<PointHistory, String> {

    @Query(
        value = "SELECT COALESCE(SUM(REMAIN_POINT), 0) FROM POINT_HISTORY WHERE MEMBER_NO = :memberNo AND POINT_TRANSACTION_CODE = '001' AND REMAIN_POINT > 0 AND END_DATE_TIME > NOW()",
        nativeQuery = true
    )
    fun selectPointBalance(memberNo: String): Long

    @Query(
        value = """
        SELECT POINT_HISTORY_NO AS pointHistoryNo
             , AMOUNT AS amount
             , POINT_TRANSACTION_CODE AS pointTransactionCode
             , POINT_TRANSACTION_REASON_CODE AS pointTransactionReasonCode
             , POINT_TRANSACTION_REASON_NO AS pointTransactionReasonNo
             , START_DATE_TIME AS startDateTime
             , END_DATE_TIME AS endDateTime
             , REMAIN_POINT AS remainPoint
             , REGIST_DATE_TIME AS createdDate
          FROM POINT_HISTORY
         WHERE MEMBER_NO = :memberNo
         ORDER BY REGIST_DATE_TIME DESC
         LIMIT :size OFFSET :offset
        """,
        nativeQuery = true
    )
    fun selectPointHistoryList(memberNo: String, size: Int, offset: Long): List<PointHistoryProjection>

    @Query(
        value = "SELECT COUNT(*) FROM POINT_HISTORY WHERE MEMBER_NO = :memberNo",
        nativeQuery = true
    )
    fun countPointHistory(memberNo: String): Long

    @Query(
        value = """
        SELECT POINT_HISTORY_NO, MEMBER_NO, AMOUNT, POINT_TRANSACTION_CODE,
               POINT_TRANSACTION_REASON_CODE, POINT_TRANSACTION_REASON_NO,
               START_DATE_TIME, END_DATE_TIME, UPPER_POINT_HISTORY_NO, REMAIN_POINT,
               REGIST_ID, REGIST_DATE_TIME, MODIFY_ID, MODIFY_DATE_TIME
          FROM POINT_HISTORY
         WHERE MEMBER_NO = :memberNo
           AND POINT_TRANSACTION_CODE = '001'
           AND REMAIN_POINT > 0
           AND END_DATE_TIME > NOW()
         ORDER BY END_DATE_TIME ASC
        """,
        nativeQuery = true
    )
    fun selectAvailablePointHistory(memberNo: String): List<PointHistory>
}
