package com.api.app.service.claim;

import com.api.app.dto.request.claim.CancelRequest;

/**
 * 클레임 서비스 인터페이스
 *
 * @author Claude
 * @version 1.0
 * @since 2025-11-01
 */
public interface ClaimService {

    /**
     * 주문 취소 처리
     *
     * @param request 주문 취소 요청 정보
     */
    void cancelOrder(CancelRequest request);
}
