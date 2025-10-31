package com.api.app.dto.request.payment;

import lombok.Builder;
import lombok.Getter;

/**
 * 이니시스 승인 요청 DTO
 *
 * @author Claude
 * @version 1.0
 * @since 2025-10-31
 */
@Getter
@Builder
public class InicisApprovalRequest {

    /**
     * 가맹점 ID
     */
    private String mid;

    /**
     * 인증 토큰
     */
    private String authToken;

    /**
     * 타임스탬프
     */
    private String timestamp;

    /**
     * 서명값 (authToken + timestamp)
     */
    private String signature;

    /**
     * 검증값 (authToken + signKey + timestamp)
     */
    private String verification;

    /**
     * 문자셋
     */
    private String charset;

    /**
     * 응답 포맷
     */
    private String format;

    /**
     * 결제 금액
     */
    private Long price;
}
