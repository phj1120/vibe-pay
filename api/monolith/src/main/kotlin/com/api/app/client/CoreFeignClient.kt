package com.api.app.client

import com.api.app.client.dto.BasketResponse
import com.api.app.client.dto.MemberValidateResponse
import com.api.app.client.dto.PointTransactionRequest
import com.api.app.common.response.ApiResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name = "core-service", url = "\${core-service.url:http://localhost:8081}")
interface CoreFeignClient {

    @GetMapping("/internal/core/members/{memberNo}")
    fun getMemberByNo(@PathVariable memberNo: String): ApiResponse<MemberValidateResponse>

    @GetMapping("/internal/core/members/email/{email}")
    fun getMemberByEmail(@PathVariable email: String): ApiResponse<MemberValidateResponse>

    @PostMapping("/internal/core/point/{memberNo}/transaction")
    fun processPointTransaction(
        @PathVariable memberNo: String,
        @RequestBody request: PointTransactionRequest
    ): ApiResponse<Void>

    @GetMapping("/internal/core/point/{memberNo}/balance")
    fun getPointBalance(@PathVariable memberNo: String): ApiResponse<Long>

    @GetMapping("/internal/core/baskets")
    fun getBasketsByNos(@RequestParam basketNos: List<String>): ApiResponse<List<BasketResponse>>
}
