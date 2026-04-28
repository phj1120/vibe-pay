package com.api.app.service.point

import com.api.app.dto.request.point.PointHistoryRequest
import com.api.app.dto.request.point.PointTransactionRequest
import com.api.app.dto.response.point.PointBalanceResponse
import com.api.app.dto.response.point.PointHistoryListResponse

interface PointService {
    fun processPointTransaction(memberNo: String, request: PointTransactionRequest)
    fun getPointBalance(memberNo: String): PointBalanceResponse
    fun getPointHistoryList(memberNo: String, request: PointHistoryRequest): PointHistoryListResponse
}
