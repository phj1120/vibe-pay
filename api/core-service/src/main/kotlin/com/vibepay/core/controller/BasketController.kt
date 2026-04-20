package com.vibepay.core.controller

import com.api.app.common.response.ApiResponse
import com.vibepay.core.dto.request.basket.BasketAddRequest
import com.vibepay.core.dto.request.basket.BasketModifyRequest
import com.vibepay.core.dto.response.basket.BasketResponse
import com.vibepay.core.service.BasketService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/baskets")
class BasketController(private val basketService: BasketService) {

    @GetMapping
    fun getBasketList(@RequestHeader("X-User-Email") email: String): ApiResponse<List<BasketResponse>> =
        ApiResponse.success(basketService.getBasketList(email))

    @PostMapping
    fun addBasket(
        @RequestHeader("X-User-Email") email: String,
        @RequestBody @Valid request: BasketAddRequest
    ): ApiResponse<String> = ApiResponse.success(basketService.addBasket(email, request))

    @PutMapping("/{basketNo}")
    fun modifyBasket(
        @RequestHeader("X-User-Email") email: String,
        @PathVariable basketNo: String,
        @RequestBody request: BasketModifyRequest
    ): ApiResponse<Void> {
        basketService.modifyBasket(email, basketNo, request)
        return ApiResponse.success()
    }

    @DeleteMapping("/{basketNo}")
    fun deleteBasket(
        @RequestHeader("X-User-Email") email: String,
        @PathVariable basketNo: String
    ): ApiResponse<Void> {
        basketService.deleteBasket(email, basketNo)
        return ApiResponse.success()
    }

    @DeleteMapping
    fun deleteBaskets(
        @RequestHeader("X-User-Email") email: String,
        @RequestParam basketNos: List<String>
    ): ApiResponse<Void> {
        basketService.deleteBaskets(email, basketNos)
        return ApiResponse.success()
    }

    @DeleteMapping("/all")
    fun deleteAllBaskets(@RequestHeader("X-User-Email") email: String): ApiResponse<Void> {
        basketService.deleteAllBaskets(email)
        return ApiResponse.success()
    }
}
