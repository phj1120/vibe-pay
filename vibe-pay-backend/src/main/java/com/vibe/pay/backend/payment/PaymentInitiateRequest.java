package com.vibe.pay.backend.payment;

public class PaymentInitiateRequest {
    private Long memberId;
    private Double amount;        // 금액(원), 정수 변환하여 사용
    private String paymentMethod;
    private Double usedMileage;   // 사용한 적립금

    // 화면에서 넘어오는 정보
    private String goodName;
    private String buyerName;
    private String buyerTel;
    private String buyerEmail;

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
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

    public Double getUsedMileage() {
        return usedMileage;
    }

    public void setUsedMileage(Double usedMileage) {
        this.usedMileage = usedMileage;
    }
}
