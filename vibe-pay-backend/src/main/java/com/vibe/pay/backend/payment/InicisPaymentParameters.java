package com.vibe.pay.backend.payment;

public class InicisPaymentParameters {
    private String mid;
    private String oid;
    private String price;
    private String timestamp;
    private String signature;
    private String mKey;
    private String version;
    private String currency;
    private String moId;
    private String goodName;
    private String buyerName;
    private String buyerTel;
    private String buyerEmail;
    private String returnUrl;
    private String closeUrl;
    private String verification;

    // 결제수단/옵션
    private String gopaymethod;
    private String acceptmethod;

    // Getters and Setters
    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getmKey() {
        return mKey;
    }

    public void setmKey(String mKey) {
        this.mKey = mKey;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }



    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getMoId() {
        return moId;
    }

    public void setMoId(String moId) {
        this.moId = moId;
    }

    public String getGoodName() {
        return goodName;
    }

    public void setGoodName(String goodName) {
        this.goodName = goodName;
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

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public String getCloseUrl() {
        return closeUrl;
    }

    public void setCloseUrl(String closeUrl) {
        this.closeUrl = closeUrl;
    }

    public String getVerification() {
        return verification;
    }

    public void setVerification(String verification) {
        this.verification = verification;
    }

    public String getGopaymethod() {
        return gopaymethod;
    }

    public void setGopaymethod(String gopaymethod) {
        this.gopaymethod = gopaymethod;
    }

    public String getAcceptmethod() {
        return acceptmethod;
    }

    public void setAcceptmethod(String acceptmethod) {
        this.acceptmethod = acceptmethod;
    }
}
