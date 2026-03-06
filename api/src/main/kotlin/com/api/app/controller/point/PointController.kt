package com.api.app.controller.point

import com.api.app.common.response.ApiResponse
import com.api.app.common.security.SecurityUtils
import com.api.app.dto.request.point.PointHistoryRequest
import com.api.app.dto.request.point.PointTransactionRequest
import com.api.app.dto.response.point.PointBalanceResponse
import com.api.app.dto.response.point.PointHistoryListResponse
import com.api.app.service.point.PointService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/point")
@PreAuthorize("isAuthenticated()")
class PointController(
    private val pointService: PointService,
    private val securityUtils: SecurityUtils
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    @PostMapping("/transaction")
    fun processPointTransaction(@RequestBody @Valid request: PointTransactionRequest): ApiResponse<Void> {
        val memberNo = securityUtils.getCurrentUserMemberNo()
        log.info("포인트 거래 요청: memberNo={}, code={}, amount={}", memberNo, request.pointTransactionCode, request.amount)
        pointService.processPointTransaction(memberNo, request)
        return ApiResponse.success()
    }

    @GetMapping("/balance")
    fun getPointBalance(): ApiResponse<PointBalanceResponse> {
        val memberNo = securityUtils.getCurrentUserMemberNo()
        return ApiResponse.success(pointService.getPointBalance(memberNo))
    }

    @GetMapping("/history")
    fun getPointHistoryList(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ApiResponse<PointHistoryListResponse> {
        val memberNo = securityUtils.getCurrentUserMemberNo()
        log.info("포인트 내역 조회 요청: memberNo={}, page={}, size={}", memberNo, page, size)
        return ApiResponse.success(pointService.getPointHistoryList(memberNo, PointHistoryRequest(page = page, size = size)))
    }
}
