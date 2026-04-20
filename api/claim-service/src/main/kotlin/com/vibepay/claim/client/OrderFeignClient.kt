package com.vibepay.claim.client

import com.api.app.common.response.ApiResponse
import com.vibepay.claim.dto.CreateCancelDetailsRequest
import com.vibepay.claim.dto.OrderCancelInfoResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name = "order-service", url = "\${order-service.url:http://localhost:8083}")
interface OrderFeignClient {

    @GetMapping("/internal/orders/{orderNo}/cancel-info")
    fun getOrderCancelInfo(
        @PathVariable orderNo: String,
        @RequestParam memberNo: String
    ): ApiResponse<OrderCancelInfoResponse>

    @PostMapping("/internal/orders/{orderNo}/cancel-details")
    fun createCancelDetails(
        @PathVariable orderNo: String,
        @RequestBody request: CreateCancelDetailsRequest
    ): ApiResponse<Void>

    @PostMapping("/internal/orders/{orderNo}/generate-claim-no")
    fun generateClaimNo(@PathVariable orderNo: String): ApiResponse<String>
}
