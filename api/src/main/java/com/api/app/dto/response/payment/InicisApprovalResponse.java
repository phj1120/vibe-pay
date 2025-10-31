package com.api.app.dto.response.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 이니시스 승인 응답 DTO
 *
 * @author Claude
 * @version 1.0
 * @since 2025-10-31
 */
@Getter
@Setter
public class InicisApprovalResponse {

    /**
     * 결과 코드 (0000: 성공)
     */
    private String resultCode;

    /**
     * 결과 메시지
     */
    private String resultMsg;

    /**
     * 승인번호
     */
    private String applNum;

    /**
     * 거래 ID (TID)
     */
    private String tid;

    /**
     * 결제금액
     */
    @JsonProperty("TotPrice")
    private String TotPrice;

    /**
     * 카드번호
     */
    @JsonProperty("CARD_Num")
    private String CARD_Num;

    /**
     * 카드사 코드
     */
    @JsonProperty("CARD_Code")
    private String CARD_Code;

    /**
     * 카드사 명
     */
    @JsonProperty("CARD_BankCode")
    private String CARD_BankCode;

    /**
     * 할부개월
     */
    @JsonProperty("CARD_Quota")
    private String CARD_Quota;

    /**
     * 승인일시
     */
    private String applDate;

    /**
     * 가맹점 ID
     */
    private String mid;

    /**
     * 주문번호
     */
    private String oid;
}
