package com.api.app.client

import com.api.app.client.dto.GoodsValidateRequest
import com.api.app.client.dto.GoodsValidateResponse
import com.api.app.common.response.ApiResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(name = "goods-service", url = "\${goods-service.url:http://localhost:8082}")
interface GoodsFeignClient {

    @PostMapping("/internal/goods/validate")
    fun validateGoods(@RequestBody request: GoodsValidateRequest): ApiResponse<GoodsValidateResponse>
}
