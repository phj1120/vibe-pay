package com.api.app.controller.order

import com.api.app.common.response.ApiResponse
import com.api.app.dto.response.order.OrderSheetResponse
import com.api.app.service.order.OrderSheetService
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/order")
@PreAuthorize("isAuthenticated()")
class OrderSheetController(private val orderSheetService: OrderSheetService) {

    private val log = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/sheet")
    fun getOrderSheet(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam basketNos: List<String>
    ): ApiResponse<OrderSheetResponse> {
        log.info("주문서 조회 요청: email={}, basketNos={}", userDetails.username, basketNos)
        return ApiResponse.success(orderSheetService.getOrderSheet(userDetails.username, basketNos))
    }
}
