package com.api.app.controller.order

import com.api.app.common.response.ApiResponse
import com.api.app.common.security.SecurityUtils
import com.api.app.dto.request.order.OrderRequest
import com.api.app.dto.response.order.CancelableOrderResponse
import com.api.app.dto.response.order.OrderCompleteResponse
import com.api.app.dto.response.order.OrderListResponse
import com.api.app.dto.response.order.OrderNumberResponse
import com.api.app.service.order.OrderService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/order")
@PreAuthorize("isAuthenticated()")
class OrderController(
    private val orderService: OrderService,
    private val securityUtils: SecurityUtils
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/generateOrderNumber")
    fun generateOrderNumber(): ApiResponse<OrderNumberResponse> {
        val orderNumber = orderService.generateOrderNumber()
        log.info("Generate order number completed. orderNumber={}", orderNumber)
        return ApiResponse.success(OrderNumberResponse(orderNumber))
    }

    @PostMapping("/order")
    fun createOrder(@Valid @RequestBody request: OrderRequest): ApiResponse<Void> {
        val memberNo = securityUtils.getCurrentUserMemberNo()
        val enrichedRequest = request.copy(memberNo = memberNo)
        log.info("Create order request received. memberNo={}, goodsCount={}", memberNo, request.goodsList.size)
        orderService.createOrder(enrichedRequest)
        return ApiResponse.success()
    }

    @GetMapping("/complete/{orderNo}")
    fun getOrderComplete(@PathVariable orderNo: String): ApiResponse<OrderCompleteResponse> {
        val memberNo = securityUtils.getCurrentUserMemberNo()
        return ApiResponse.success(orderService.getOrderComplete(orderNo, memberNo))
    }

    @GetMapping("/list")
    fun getOrderList(): ApiResponse<List<OrderListResponse>> {
        val memberNo = securityUtils.getCurrentUserMemberNo()
        return ApiResponse.success(orderService.getOrderList(memberNo))
    }

    @GetMapping("/cancelable/{orderNo}")
    fun getCancelableOrders(@PathVariable orderNo: String): ApiResponse<CancelableOrderResponse> {
        val memberNo = securityUtils.getCurrentUserMemberNo()
        return ApiResponse.success(orderService.getCancelableOrders(orderNo, memberNo))
    }
}
