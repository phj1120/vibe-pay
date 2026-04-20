package com.vibepay.payment.client

import com.api.app.common.response.ApiResponse
import com.vibepay.payment.dto.PointTransactionRequest
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(name = "core-service", url = "\${core-service.url:http://localhost:8081}")
interface CoreFeignClient {

    @PostMapping("/internal/core/point/{memberNo}/transaction")
    fun processPointTransaction(
        @PathVariable memberNo: String,
        @RequestBody request: PointTransactionRequest
    ): ApiResponse<Void>
}
