package com.vibepay.payment.controller

import com.api.app.common.response.ApiResponse
import com.vibepay.payment.dto.PaymentInitiateRequest
import com.vibepay.payment.dto.PaymentInitiateResponse
import com.vibepay.payment.service.PaymentInitiateService
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/payments")
@PreAuthorize("isAuthenticated()")
class PaymentController(private val paymentInitiateService: PaymentInitiateService) {

    @PostMapping("/initiate")
    fun initiatePayment(@Valid @RequestBody request: PaymentInitiateRequest): ApiResponse<PaymentInitiateResponse> =
        ApiResponse.success(paymentInitiateService.initiate(request))
}
