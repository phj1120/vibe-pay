package com.api.app.repository.rwdb.point

import com.api.app.entity.PointHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query


interface PointHistoryTrxRepository : JpaRepository<PointHistory, String> {

    @Modifying
    @Query(
        value = "UPDATE POINT_HISTORY SET REMAIN_POINT = :remainPoint, MODIFY_ID = :modifyId, MODIFY_DATE_TIME = NOW() WHERE POINT_HISTORY_NO = :pointHistoryNo",
        nativeQuery = true
    )
    fun updateRemainPoint(pointHistoryNo: String, remainPoint: Long, modifyId: String): Int
}
