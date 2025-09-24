package com.vibe.pay.backend.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 나이스페이 승인 API 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NicePayConfirmResponse {

    private String resultCode;      // 4 byte 결과 코드 (3001: 신용카드 성공)
    private String resultMsg;       // 100 byte 결과 메시지 (euc-kr)
    @JsonProperty("TID")
    private String tID;             // 30 byte 거래ID
    private String amt;             // 12 byte 금액
    @JsonProperty("MID")
    private String mID;             // 10 byte 가맹점 ID
    private String moid;            // 64 byte 가맹점 주문번호
    private String signature;       // 500 byte hex(sha256(TID + MID + Amt + MerchantKey))
    private String buyerEmail;      // 60 byte 옵션 메일주소
    private String buyerTel;        // 20 byte 옵션 구매자 연락처
    private String buyerName;       // 30 byte 옵션 구매자명
    private String goodsName;       // 40 byte 상품명
    private String authCode;        // 30 byte 옵션 승인번호 (신용카드, 계좌이체, 휴대폰)
    private String authDate;        // 12 byte YYMMDDHHMMSS, 승인일시
    private String payMethod;       // 10 byte 결제수단 (CARD, BANK, VBANK, CELLPHONE)
    private String mallReserved;    // 500 byte 가맹점 여분 필드
}