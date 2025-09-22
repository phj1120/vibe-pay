package com.vibe.pay.backend.order;

import java.time.LocalDateTime;

public class Order {

    private String orderId;
    private Integer ordSeq;
    private Integer ordProcSeq;
    private String claimId;
    private Long memberId;
    private LocalDateTime orderDate;
    private Double totalAmount;
    private String status;


    public Order() {
    }

    public Order(Long memberId, Double totalAmount, String status) {
        this.memberId = memberId;
        this.ordSeq = 1; // 기본값
        this.ordProcSeq = 1; // 기본값
        this.orderDate = LocalDateTime.now();
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Integer getOrdSeq() {
        return ordSeq;
    }

    public void setOrdSeq(Integer ordSeq) {
        this.ordSeq = ordSeq;
    }

    public Integer getOrdProcSeq() {
        return ordProcSeq;
    }

    public void setOrdProcSeq(Integer ordProcSeq) {
        this.ordProcSeq = ordProcSeq;
    }

    public String getClaimId() {
        return claimId;
    }

    public void setClaimId(String claimId) {
        this.claimId = claimId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }



    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


}