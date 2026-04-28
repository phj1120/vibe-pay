package com.api.app.controller.goods

import com.api.app.common.response.ApiResponse
import com.api.app.dto.request.goods.GoodsModifyRequest
import com.api.app.dto.request.goods.GoodsRegisterRequest
import com.api.app.dto.request.goods.GoodsSearchRequest
import com.api.app.dto.response.goods.GoodsDetailResponse
import com.api.app.dto.response.goods.GoodsPageResponse
import com.api.app.service.goods.GoodsService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/goods")
class GoodsController(private val goodsService: GoodsService) {

    private val log = LoggerFactory.getLogger(this::class.java)

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    fun registerGoods(@RequestBody @Valid request: GoodsRegisterRequest): ApiResponse<String> {
        log.info("상품 등록 요청: goodsName={}", request.goodsName)
        return ApiResponse.success(goodsService.registerGoods(request))
    }

    @PutMapping("/{goodsNo}")
    @PreAuthorize("isAuthenticated()")
    fun modifyGoods(
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
    @PreAuthorize("isAuthenticated()")
    fun deleteGoods(@PathVariable goodsNo: String): ApiResponse<Void> {
        goodsService.deleteGoods(goodsNo)
        return ApiResponse.success()
    }
}
