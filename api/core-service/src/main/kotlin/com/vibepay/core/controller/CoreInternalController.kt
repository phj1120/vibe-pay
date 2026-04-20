package com.vibepay.core.controller

import com.api.app.common.exception.ApiError
import com.api.app.common.exception.ApiException
import com.api.app.common.response.ApiResponse
import com.vibepay.core.dto.internal.MemberValidateResponse
import com.vibepay.core.dto.request.point.PointTransactionRequest
import com.vibepay.core.dto.response.point.PointBalanceResponse
import com.vibepay.core.dto.response.basket.BasketResponse
import com.vibepay.core.service.BasketService
import com.vibepay.core.service.MemberService
import com.vibepay.core.service.PointService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/core")
class CoreInternalController(
    private val memberService: MemberService,
    private val pointService: PointService,
    private val basketService: BasketService
) {

    @GetMapping("/members/email/{email}")
    fun getMemberByEmail(@PathVariable email: String): ApiResponse<MemberValidateResponse> {
        val member = memberService.findByEmail(email) ?: throw ApiException(ApiError.DATA_NOT_FOUND, "회원을 찾을 수 없습니다")
        return ApiResponse.success(MemberValidateResponse(
            memberNo = member.memberNo, email = member.email,
            memberStatusCode = member.memberStatusCode, memberName = member.memberName
        ))
    }

    @GetMapping("/members/{memberNo}")
    fun getMemberByNo(@PathVariable memberNo: String): ApiResponse<MemberValidateResponse> {
        val member = memberService.findByMemberNo(memberNo) ?: throw ApiException(ApiError.DATA_NOT_FOUND, "회원을 찾을 수 없습니다")
        return ApiResponse.success(MemberValidateResponse(
            memberNo = member.memberNo, email = member.email,
            memberStatusCode = member.memberStatusCode, memberName = member.memberName
        ))
    }

    @PostMapping("/point/{memberNo}/transaction")
    fun processPointTransaction(
        @PathVariable memberNo: String,
        @RequestBody @Valid request: PointTransactionRequest
    ): ApiResponse<Void> {
        pointService.processPointTransaction(memberNo, request)
        return ApiResponse.success()
    }

    @GetMapping("/point/{memberNo}/balance")
    fun getPointBalance(@PathVariable memberNo: String): ApiResponse<PointBalanceResponse> =
        ApiResponse.success(pointService.getPointBalance(memberNo))

    @GetMapping("/baskets")
    fun getBasketsByNos(@RequestParam basketNos: List<String>): ApiResponse<List<BasketResponse>> =
        ApiResponse.success(basketService.getBasketsByNos(basketNos))
}
