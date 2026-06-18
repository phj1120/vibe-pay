package com.api.app.repository.rodb.point

import com.api.app.entity.PointHistory

interface PointHistoryRepositoryCustom {
    fun selectPointBalance(memberNo: String): Long
    fun selectPointHistoryList(memberNo: String, size: Int, offset: Long): List<PointHistory>
    fun countPointHistory(memberNo: String): Long
    fun selectAvailablePointHistory(memberNo: String): List<PointHistory>
}
