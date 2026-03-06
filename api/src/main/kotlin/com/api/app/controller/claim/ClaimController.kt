package com.api.app.controller.claim

import com.api.app.common.response.ApiResponse
import com.api.app.common.security.SecurityUtils
import com.api.app.dto.request.claim.CancelRequest
import com.api.app.service.claim.ClaimService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/claim")
@PreAuthorize("isAuthenticated()")
class ClaimController(
    private val claimService: ClaimService,
    private val securityUtils: SecurityUtils
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    @PostMapping("/cancel")
    fun cancelOrder(@Valid @RequestBody request: CancelRequest): ApiResponse<Void> {
        val memberNo = securityUtils.getCurrentUserMemberNo()
        log.info("Cancel order request received. targetCount={}", request.targets.size)
        claimService.cancelOrder(request.copy(memberNo = memberNo))
        log.info("Cancel order completed. memberNo={}", memberNo)
        return ApiResponse.success()
    }
}
