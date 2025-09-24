package com.vibe.pay.backend.payment.dto;

/**
 * 나이스페이 승인 API 요청 DTO
 */
public class NicePayConfirmRequest {

    private String TID;         // 30 byte 필수 거래번호 (인증 응답 TxTid 사용)
    private String AuthToken;   // 40 byte 필수 인증 TOKEN
    private String MID;         // 10 byte 필수 가맹점아이디
    private String Amt;         // 12 byte 필수 금액 (숫자만)
    private String EdiDate;     // 14 byte 필수 전문생성일시 (YYYYMMDDHHMMSS)
    private String SignData;    // 256 byte 필수 hex(sha256(AuthToken + MID + Amt + EdiDate + MerchantKey))

    public String getTID() {
        return TID;
    }

    public void setTID(String TID) {
        this.TID = TID;
    }

    public String getAuthToken() {
        return AuthToken;
    }

    public void setAuthToken(String authToken) {
        AuthToken = authToken;
    }

    public String getMID() {
        return MID;
    }

    public void setMID(String MID) {
        this.MID = MID;
    }

    public String getAmt() {
        return Amt;
    }

    public void setAmt(String amt) {
        Amt = amt;
    }

    public String getEdiDate() {
        return EdiDate;
    }

    public void setEdiDate(String ediDate) {
        EdiDate = ediDate;
    }

    public String getSignData() {
        return SignData;
    }

    public void setSignData(String signData) {
        SignData = signData;
    }

    @Override
    public String toString() {
        return "NicePayConfirmRequest{" +
                "TID='" + TID + '\'' +
                ", AuthToken='" + AuthToken + '\'' +
                ", MID='" + MID + '\'' +
                ", Amt='" + Amt + '\'' +
                ", EdiDate='" + EdiDate + '\'' +
                ", SignData='" + SignData + '\'' +
                '}';
    }
}