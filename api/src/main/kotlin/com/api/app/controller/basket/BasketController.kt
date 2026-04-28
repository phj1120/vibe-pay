package com.api.app.controller.basket

import com.api.app.common.response.ApiResponse
import com.api.app.dto.request.basket.BasketAddRequest
import com.api.app.dto.request.basket.BasketModifyRequest
import com.api.app.dto.response.basket.BasketResponse
import com.api.app.service.basket.BasketService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/baskets")
@PreAuthorize("isAuthenticated()")
class BasketController(private val basketService: BasketService) {

    private val log = LoggerFactory.getLogger(this::class.java)

    @GetMapping
    fun getBasketList(@AuthenticationPrincipal userDetails: UserDetails): ApiResponse<List<BasketResponse>> =
        ApiResponse.success(basketService.getBasketList(userDetails.username))

    @PostMapping
    fun addBasket(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody @Valid request: BasketAddRequest
    ): ApiResponse<String> {
        log.info("장바구니 추가 요청: email={}, goodsNo={}", userDetails.username, request.goodsNo)
        return ApiResponse.success(basketService.addBasket(userDetails.username, request))
    }

    @PutMapping("/{basketNo}")
    fun modifyBasket(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable basketNo: String,
        @RequestBody @Valid request: BasketModifyRequest
    ): ApiResponse<Void> {
        basketService.modifyBasket(userDetails.username, basketNo, request)
        return ApiResponse.success()
    }

    @DeleteMapping("/{basketNo}")
    fun deleteBasket(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable basketNo: String
    ): ApiResponse<Void> {
        basketService.deleteBasket(userDetails.username, basketNo)
        return ApiResponse.success()
    }

    @DeleteMapping
    fun deleteBaskets(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam basketNos: List<String>
    ): ApiResponse<Void> {
        basketService.deleteBaskets(userDetails.username, basketNos)
        return ApiResponse.success()
    }

    @DeleteMapping("/all")
    fun deleteAllBaskets(@AuthenticationPrincipal userDetails: UserDetails): ApiResponse<Void> {
        basketService.deleteAllBaskets(userDetails.username)
        return ApiResponse.success()
    }
}
