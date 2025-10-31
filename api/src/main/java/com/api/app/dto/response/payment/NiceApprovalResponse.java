package com.api.app.dto.response.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 나이스 승인 응답 DTO
 *
 * @author Claude
 * @version 1.0
 * @since 2025-10-31
 */
@Getter
@Setter
public class NiceApprovalResponse {

    /**
     * 결과 코드 (3001: 신용카드 성공)
     */
    @JsonProperty("ResultCode")
    private String ResultCode;

    /**
     * 결과 메시지
     */
    @JsonProperty("ResultMsg")
    private String ResultMsg;

    /**
     * 승인번호
     */
    @JsonProperty("AuthCode")
    private String AuthCode;

    /**
     * 거래 ID (TID)
     */
    @JsonProperty("TID")
    private String TID;

    /**
     * 승인일시
     */
    @JsonProperty("AuthDate")
    private String AuthDate;

    /**
     * 결제금액
     */
    @JsonProperty("Amt")
    private String Amt;

    /**
     * 카드번호
     */
    @JsonProperty("CardNo")
    private String CardNo;

    /**
     * 카드사 코드
     */
    @JsonProperty("CardCode")
    private String CardCode;

    /**
     * 카드사 명
     */
    @JsonProperty("CardName")
    private String CardName;

    /**
     * 할부개월
     */
    @JsonProperty("CardQuota")
    private String CardQuota;

    /**
     * 카드 이자
     */
    @JsonProperty("CardInterest")
    private String CardInterest;

    /**
     * 매입사 코드
     */
    @JsonProperty("AcquCardCode")
    private String AcquCardCode;

    /**
     * 매입사 명
     */
    @JsonProperty("AcquCardName")
    private String AcquCardName;

    /**
     * 카드 구분 (0:신용, 1:체크)
     */
    @JsonProperty("CardCl")
    private String CardCl;

    /**
     * 카드 타입 (01:개인, 02:법인)
     */
    @JsonProperty("CardType")
    private String CardType;

    /**
     * 결제수단
     */
    @JsonProperty("PayMethod")
    private String PayMethod;

    /**
     * 가맹점 ID
     */
    @JsonProperty("MID")
    private String MID;

    /**
     * 주문번호
     */
    @JsonProperty("Moid")
    private String Moid;

    /**
     * 구매자 이메일
     */
    @JsonProperty("BuyerEmail")
    private String BuyerEmail;

    /**
     * 구매자 전화번호
     */
    @JsonProperty("BuyerTel")
    private String BuyerTel;

    /**
     * 구매자 명
     */
    @JsonProperty("BuyerName")
    private String BuyerName;

    /**
     * 상품명
     */
    @JsonProperty("GoodsName")
    private String GoodsName;

    /**
     * 메시지 출처
     */
    @JsonProperty("MsgSource")
    private String MsgSource;

    /**
     * 서명값
     */
    @JsonProperty("Signature")
    private String Signature;

    /**
     * 가맹점 예비필드
     */
    @JsonProperty("MallReserved")
    private String MallReserved;
}
