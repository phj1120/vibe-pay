package com.vibe.pay.backend.order;

public class PaymentMethodRequest {
    private String paymentMethod; // CREDIT_CARD, POINT
    private Long amount; // 해당 결제 수단으로 결제할 금액

    // 결제 정보 (2단계에서 받은 결제 응답 정보)
    private String authToken;
    private String authUrl;
    private String mid;
    private String netCancelUrl;
    private String pgCompany; // PG사 정보

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getAuthUrl() {
        return authUrl;
    }

    public void setAuthUrl(String authUrl) {
        this.authUrl = authUrl;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getNetCancelUrl() {
        return netCancelUrl;
    }

    public void setNetCancelUrl(String netCancelUrl) {
        this.netCancelUrl = netCancelUrl;
    }

    public String getPgCompany() {
        return pgCompany;
    }

    public void setPgCompany(String pgCompany) {
        this.pgCompany = pgCompany;
    }
}