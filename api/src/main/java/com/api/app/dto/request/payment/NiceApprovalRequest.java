package com.api.app.dto.request.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

/**
 * 나이스 승인 요청 DTO
 *
 * @author Claude
 * @version 1.0
 * @since 2025-10-31
 */
@Getter
@Builder
public class NiceApprovalRequest {

    /**
     * 거래번호 (인증 응답 TxTid)
     */
    @JsonProperty("TID")
    private String tid;

    /**
     * 인증 토큰
     */
    @JsonProperty("AuthToken")
    private String authToken;

    /**
     * 가맹점 ID
     */
    @JsonProperty("MID")
    private String mid;

    /**
     * 결제금액
     */
    @JsonProperty("Amt")
    private String amt;

    /**
     * 전문생성일시
     */
    @JsonProperty("EdiDate")
    private String ediDate;

    /**
     * 위변조 검증 데이터
     */
    @JsonProperty("SignData")
    private String signData;

    /**
     * 문자셋
     */
    @JsonProperty("CharSet")
    private String charSet;

    /**
     * 전문타입
     */
    @JsonProperty("EdiType")
    private String ediType;
}
