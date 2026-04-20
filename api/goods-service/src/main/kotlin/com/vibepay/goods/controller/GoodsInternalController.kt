package com.vibepay.goods.controller

import com.api.app.common.response.ApiResponse
import com.vibepay.goods.dto.internal.GoodsValidateRequest
import com.vibepay.goods.dto.internal.GoodsValidateResponse
import com.vibepay.goods.service.GoodsService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/goods")
class GoodsInternalController(private val goodsService: GoodsService) {

    @PostMapping("/validate")
    fun validateGoods(@RequestBody request: GoodsValidateRequest): ApiResponse<GoodsValidateResponse> =
        ApiResponse.success(goodsService.validateGoods(request))
}
