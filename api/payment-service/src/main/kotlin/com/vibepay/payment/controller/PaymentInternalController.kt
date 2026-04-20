package com.vibepay.payment.controller

import com.api.app.common.response.ApiResponse
import com.vibepay.payment.dto.CancelByOrderRequest
import com.vibepay.payment.service.PaymentProcessingService
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/payments")
class PaymentInternalController(private val paymentProcessingService: PaymentProcessingService) {

    @PostMapping("/{orderNo}/cancel")
    fun cancelByOrder(
        @PathVariable orderNo: String,
        @RequestBody request: CancelByOrderRequest
    ): ApiResponse<Void> {
        paymentProcessingService.cancelByOrder(orderNo, request.claimNo, request.memberNo, request.cancelAmounts)
        return ApiResponse.success()
    }
}
