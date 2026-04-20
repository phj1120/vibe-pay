package com.vibepay.core.controller

import com.api.app.common.response.ApiResponse
import com.vibepay.core.dto.request.point.PointHistoryRequest
import com.vibepay.core.dto.response.point.PointBalanceResponse
import com.vibepay.core.dto.response.point.PointHistoryListResponse
import com.vibepay.core.service.MemberService
import com.vibepay.core.service.PointService
import com.api.app.common.exception.ApiError
import com.api.app.common.exception.ApiException
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/point")
class PointController(
    private val pointService: PointService,
    private val memberService: MemberService
) {

    @GetMapping("/balance")
    fun getBalance(@RequestHeader("X-User-Email") email: String): ApiResponse<PointBalanceResponse> {
        val memberNo = memberService.findByEmail(email)?.memberNo ?: throw ApiException(ApiError.DATA_NOT_FOUND, "회원 정보를 찾을 수 없습니다")
        return ApiResponse.success(pointService.getPointBalance(memberNo))
    }

    @GetMapping("/history")
    fun getHistory(
        @RequestHeader("X-User-Email") email: String,
        @ModelAttribute @Valid request: PointHistoryRequest
    ): ApiResponse<PointHistoryListResponse> {
        val memberNo = memberService.findByEmail(email)?.memberNo ?: throw ApiException(ApiError.DATA_NOT_FOUND, "회원 정보를 찾을 수 없습니다")
        return ApiResponse.success(pointService.getPointHistoryList(memberNo, request))
    }
}
