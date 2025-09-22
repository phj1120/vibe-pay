package com.vibe.pay.backend.payment;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InicisRefundResponse {
    @JsonProperty("resultCode")
    private String resultCode;

    @JsonProperty("resultMsg")
    private String resultMsg;

    @JsonProperty("tid")
    private String tid;

    @JsonProperty("cancelDate")
    private String cancelDate;

    @JsonProperty("cancelTime")
    private String cancelTime;

    @JsonProperty("CSHR_ResultCode")
    private String cshrResultCode;

    @JsonProperty("CSHR_ResultMsg")
    private String cshrResultMsg;

    // Constructors
    public InicisRefundResponse() {}

    // Getters and Setters
    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getCancelDate() {
        return cancelDate;
    }

    public void setCancelDate(String cancelDate) {
        this.cancelDate = cancelDate;
    }

    public String getCancelTime() {
        return cancelTime;
    }

    public void setCancelTime(String cancelTime) {
        this.cancelTime = cancelTime;
    }

    public String getCshrResultCode() {
        return cshrResultCode;
    }

    public void setCshrResultCode(String cshrResultCode) {
        this.cshrResultCode = cshrResultCode;
    }

    public String getCshrResultMsg() {
        return cshrResultMsg;
    }

    public void setCshrResultMsg(String cshrResultMsg) {
        this.cshrResultMsg = cshrResultMsg;
    }

    @Override
    public String toString() {
        return "InicisRefundResponse{" +
                "resultCode='" + resultCode + '\'' +
                ", resultMsg='" + resultMsg + '\'' +
                ", tid='" + tid + '\'' +
                ", cancelDate='" + cancelDate + '\'' +
                ", cancelTime='" + cancelTime + '\'' +
                ", cshrResultCode='" + cshrResultCode + '\'' +
                ", cshrResultMsg='" + cshrResultMsg + '\'' +
                '}';
    }
}