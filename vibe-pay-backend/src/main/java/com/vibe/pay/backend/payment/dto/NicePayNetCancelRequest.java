package com.vibe.pay.backend.payment.dto;

/**
 * 나이스페이 망취소 API 요청 DTO
 */
public class NicePayNetCancelRequest {

    private String TID;         // 30 byte 필수 거래 ID
    private String AuthToken;   // 40 byte 필수 인증 TOKEN
    private String MID;         // 10 byte 필수 가맹점 ID
    private String Amt;         // 12 byte 금액
    private String EdiDate;     // 14 byte 필수 전문생성일시 (YYYYMMDDHHMMSS)
    private String NetCancel;   // 1 byte 필수 1 고정 (망취소 여부)
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

    public String getNetCancel() {
        return NetCancel;
    }

    public void setNetCancel(String netCancel) {
        NetCancel = netCancel;
    }

    public String getSignData() {
        return SignData;
    }

    public void setSignData(String signData) {
        SignData = signData;
    }

    @Override
    public String toString() {
        return "NicePayNetCancelRequest{" +
                "TID='" + TID + '\'' +
                ", AuthToken='" + AuthToken + '\'' +
                ", MID='" + MID + '\'' +
                ", Amt='" + Amt + '\'' +
                ", EdiDate='" + EdiDate + '\'' +
                ", NetCancel='" + NetCancel + '\'' +
                ", SignData='" + SignData + '\'' +
                '}';
    }
}