package com.vibepay.claim.controller

import com.api.app.common.response.ApiResponse
import com.vibepay.claim.dto.CancelRequest
import com.vibepay.claim.service.ClaimService
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/claim")
@PreAuthorize("isAuthenticated()")
class ClaimController(private val claimService: ClaimService) {

    @PostMapping("/cancel")
    fun cancelOrder(@Valid @RequestBody request: CancelRequest): ApiResponse<Void> {
        claimService.cancelOrder(request)
        return ApiResponse.success()
    }
}
