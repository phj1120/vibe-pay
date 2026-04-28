package com.api.app.controller.payment

import com.api.app.common.response.ApiResponse
import com.api.app.dto.request.payment.PaymentInitiateRequest
import com.api.app.dto.response.payment.PaymentInitiateResponse
import com.api.app.service.payment.PaymentService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/payments")
@PreAuthorize("isAuthenticated()")
class PaymentController(private val paymentService: PaymentService) {

    private val log = LoggerFactory.getLogger(this::class.java)

    @PostMapping("/initiate")
    fun initiatePayment(@Valid @RequestBody request: PaymentInitiateRequest): ApiResponse<PaymentInitiateResponse> {
        log.info("Payment initiate request received. orderNumber={}, amount={}", request.orderNumber, request.amount)
        val response = paymentService.initiatePayment(request)
        log.info("Payment initiate completed. pgType={}", response.pgType)
        return ApiResponse.success(response)
    }
}
