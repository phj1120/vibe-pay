package com.api.app.service.claim

import com.api.app.dto.request.claim.CancelRequest

interface ClaimService {
    fun cancelOrder(request: CancelRequest)
}
