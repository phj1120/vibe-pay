package com.vibepay.goods.controller

import com.api.app.common.response.ApiResponse
import com.vibepay.goods.dto.request.GoodsModifyRequest
import com.vibepay.goods.dto.request.GoodsRegisterRequest
import com.vibepay.goods.dto.request.GoodsSearchRequest
import com.vibepay.goods.dto.response.GoodsDetailResponse
import com.vibepay.goods.dto.response.GoodsPageResponse
import com.vibepay.goods.service.GoodsService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/goods")
class GoodsController(private val goodsService: GoodsService) {

    @PostMapping
    fun registerGoods(
        @RequestHeader("X-User-Email") userEmail: String,
        @RequestBody @Valid request: GoodsRegisterRequest
    ): ApiResponse<String> = ApiResponse.success(goodsService.registerGoods(request))

    @PutMapping("/{goodsNo}")
    fun modifyGoods(
        @RequestHeader("X-User-Email") userEmail: String,
        @PathVariable goodsNo: String,
        @RequestBody @Valid request: GoodsModifyRequest
    ): ApiResponse<Void> {
        goodsService.modifyGoods(goodsNo, request)
        return ApiResponse.success()
    }

    @GetMapping
    fun getGoodsList(@ModelAttribute @Valid request: GoodsSearchRequest): ApiResponse<GoodsPageResponse> =
        ApiResponse.success(goodsService.getGoodsList(request))

    @GetMapping("/{goodsNo}")
    fun getGoodsDetail(@PathVariable goodsNo: String): ApiResponse<GoodsDetailResponse> =
        ApiResponse.success(goodsService.getGoodsDetail(goodsNo))

    @DeleteMapping("/{goodsNo}")
    fun deleteGoods(
        @RequestHeader("X-User-Email") userEmail: String,
        @PathVariable goodsNo: String
    ): ApiResponse<Void> {
        goodsService.deleteGoods(goodsNo)
        return ApiResponse.success()
    }
}
