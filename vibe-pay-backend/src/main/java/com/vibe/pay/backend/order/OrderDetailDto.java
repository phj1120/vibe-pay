package com.vibe.pay.backend.order;

import com.vibe.pay.backend.payment.Payment;

import java.time.LocalDateTime;
import java.util.List;

public class OrderDetailDto {
    private String orderId;
    private Integer ordSeq;
    private Integer ordProcSeq;
    private String claimId;
    private Long memberId;
    private LocalDateTime orderDate;
    private Double totalAmount;
    private String status;

    // 주문 상품 정보
    private List<OrderItemDto> orderItems;

    // 결제 정보 (카드 + 포인트)
    private List<Payment> payments;

    // 주문 처리 이력 (주문 + 취소)
    private List<Order> orderProcesses;

    // 기본 생성자
    public OrderDetailDto() {}

    // Order 객체로부터 생성하는 생성자
    public OrderDetailDto(Order order) {
        this.orderId = order.getOrderId();
        this.ordSeq = order.getOrdSeq();
        this.ordProcSeq = order.getOrdProcSeq();
        this.claimId = order.getClaimId();
        this.memberId = order.getMemberId();
        this.orderDate = order.getOrderDate();
        this.totalAmount = order.getTotalAmount();
        this.status = order.getStatus();
    }

    // Getters and Setters
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

    public List<OrderItemDto> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItemDto> orderItems) {
        this.orderItems = orderItems;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }

    public List<Order> getOrderProcesses() {
        return orderProcesses;
    }

    public void setOrderProcesses(List<Order> orderProcesses) {
        this.orderProcesses = orderProcesses;
    }
}