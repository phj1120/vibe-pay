package com.vibepay.core.service

import com.vibepay.core.dto.request.point.PointHistoryRequest
import com.vibepay.core.dto.request.point.PointTransactionRequest
import com.vibepay.core.dto.response.point.PointBalanceResponse
import com.vibepay.core.dto.response.point.PointHistoryListResponse

interface PointService {
    fun processPointTransaction(memberNo: String, request: PointTransactionRequest)
    fun getPointBalance(memberNo: String): PointBalanceResponse
    fun getPointHistoryList(memberNo: String, request: PointHistoryRequest): PointHistoryListResponse
}
