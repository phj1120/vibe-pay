package com.api.app.dto.request.claim;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 클레임 대상 요청 DTO
 *
 * @author Claude
 * @version 1.0
 * @since 2025-11-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimTargetRequest {

    /**
     * 주문번호
     */
    private String orderNo;

    /**
     * 주문순번
     */
    private Long orderSequence;

    /**
     * 주문처리순번
     */
    private Long orderProcessSequence;
}
