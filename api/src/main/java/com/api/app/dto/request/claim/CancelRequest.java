package com.api.app.dto.request.claim;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 주문 취소 요청 DTO
 *
 * @author Claude
 * @version 1.0
 * @since 2025-11-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelRequest {

    /**
     * 회원번호 (토큰에서 추출)
     */
    private String memberNo;

    /**
     * 취소 대상 목록
     */
    private List<ClaimTargetRequest> targets;
}
