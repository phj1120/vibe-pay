package com.vibe.pay.backend.payment;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InicisApprovalResponse {
    
    @JsonProperty("resultCode")
    private String resultCode;
    
    @JsonProperty("resultMsg")
    private String resultMsg;
    
    @JsonProperty("TotPrice")
    private String totPrice;
    
    @JsonProperty("tid")
    private String tid;
    
    @JsonProperty("MOID")
    private String moid;
    
    @JsonProperty("applDate")
    private String applDate;
    
    @JsonProperty("applTime")
    private String applTime;
    
    @JsonProperty("applNum")
    private String applNum;
    
    @JsonProperty("mid")
    private String mid;
    
    @JsonProperty("goodName")
    private String goodName;
    
    @JsonProperty("goodsName")
    private String goodsName;
    
    @JsonProperty("buyerName")
    private String buyerName;
    
    @JsonProperty("buyerTel")
    private String buyerTel;
    
    @JsonProperty("buyerEmail")
    private String buyerEmail;
    
    @JsonProperty("payMethod")
    private String payMethod;
    
    @JsonProperty("currency")
    private String currency;
    
    @JsonProperty("payDevice")
    private String payDevice;
    
    // 카드 관련 정보
    @JsonProperty("CARD_Quota")
    private String cardQuota;
    
    @JsonProperty("CARD_IssuerName")
    private String cardIssuerName;
    
    @JsonProperty("CARD_Num")
    private String cardNum;
    
    @JsonProperty("CARD_ApplPrice")
    private String cardApplPrice;
    
    @JsonProperty("CARD_Code")
    private String cardCode;
    
    @JsonProperty("CARD_BankCode")
    private String cardBankCode;
    
    @JsonProperty("CARD_Interest")
    private String cardInterest;
    
    @JsonProperty("P_FN_NM")
    private String pFnNm;
    
    @JsonProperty("CARD_PurchaseName")
    private String cardPurchaseName;
    
    @JsonProperty("authSignature")
    private String authSignature;

    // Constructors
    public InicisApprovalResponse() {
    }

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

    public String getTotPrice() {
        return totPrice;
    }

    public void setTotPrice(String totPrice) {
        this.totPrice = totPrice;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getMoid() {
        return moid;
    }

    public void setMoid(String moid) {
        this.moid = moid;
    }

    public String getApplDate() {
        return applDate;
    }

    public void setApplDate(String applDate) {
        this.applDate = applDate;
    }

    public String getApplTime() {
        return applTime;
    }

    public void setApplTime(String applTime) {
        this.applTime = applTime;
    }

    public String getApplNum() {
        return applNum;
    }

    public void setApplNum(String applNum) {
        this.applNum = applNum;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getGoodName() {
        return goodName;
    }

    public void setGoodName(String goodName) {
        this.goodName = goodName;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getBuyerTel() {
        return buyerTel;
    }

    public void setBuyerTel(String buyerTel) {
        this.buyerTel = buyerTel;
    }

    public String getBuyerEmail() {
        return buyerEmail;
    }

    public void setBuyerEmail(String buyerEmail) {
        this.buyerEmail = buyerEmail;
    }

    public String getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPayDevice() {
        return payDevice;
    }

    public void setPayDevice(String payDevice) {
        this.payDevice = payDevice;
    }

    public String getCardQuota() {
        return cardQuota;
    }

    public void setCardQuota(String cardQuota) {
        this.cardQuota = cardQuota;
    }

    public String getCardIssuerName() {
        return cardIssuerName;
    }

    public void setCardIssuerName(String cardIssuerName) {
        this.cardIssuerName = cardIssuerName;
    }

    public String getCardNum() {
        return cardNum;
    }

    public void setCardNum(String cardNum) {
        this.cardNum = cardNum;
    }

    public String getCardApplPrice() {
        return cardApplPrice;
    }

    public void setCardApplPrice(String cardApplPrice) {
        this.cardApplPrice = cardApplPrice;
    }

    public String getCardCode() {
        return cardCode;
    }

    public void setCardCode(String cardCode) {
        this.cardCode = cardCode;
    }

    public String getCardBankCode() {
        return cardBankCode;
    }

    public void setCardBankCode(String cardBankCode) {
        this.cardBankCode = cardBankCode;
    }

    public String getCardInterest() {
        return cardInterest;
    }

    public void setCardInterest(String cardInterest) {
        this.cardInterest = cardInterest;
    }

    public String getpFnNm() {
        return pFnNm;
    }

    public void setpFnNm(String pFnNm) {
        this.pFnNm = pFnNm;
    }

    public String getCardPurchaseName() {
        return cardPurchaseName;
    }

    public void setCardPurchaseName(String cardPurchaseName) {
        this.cardPurchaseName = cardPurchaseName;
    }

    public String getAuthSignature() {
        return authSignature;
    }

    public void setAuthSignature(String authSignature) {
        this.authSignature = authSignature;
    }

    @Override
    public String toString() {
        return "InicisApprovalResponse{" +
                "resultCode='" + resultCode + '\'' +
                ", resultMsg='" + resultMsg + '\'' +
                ", totPrice='" + totPrice + '\'' +
                ", tid='" + tid + '\'' +
                ", moid='" + moid + '\'' +
                ", applDate='" + applDate + '\'' +
                ", applTime='" + applTime + '\'' +
                ", mid='" + mid + '\'' +
                ", goodName='" + goodName + '\'' +
                ", buyerName='" + buyerName + '\'' +
                ", payMethod='" + payMethod + '\'' +
                '}';
    }
}
