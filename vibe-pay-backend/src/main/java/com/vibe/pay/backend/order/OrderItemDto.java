package com.vibe.pay.backend.order;

public class OrderItemDto {
    private Long orderItemId;
    private String orderId;
    private Integer ordSeq;
    private Integer ordProcSeq;
    private Long productId;
    private String productName;
    private Double priceAtOrder;
    private Integer quantity;
    private Double totalPrice;

    // 기본 생성자
    public OrderItemDto() {}

    // OrderItem과 Product 정보로부터 생성하는 생성자
    public OrderItemDto(OrderItem orderItem, String productName) {
        this.orderItemId = orderItem.getOrderItemId();
        this.orderId = orderItem.getOrderId();
        this.ordSeq = orderItem.getOrdSeq();
        this.ordProcSeq = orderItem.getOrdProcSeq();
        this.productId = orderItem.getProductId();
        this.productName = productName;
        this.priceAtOrder = orderItem.getPriceAtOrder();
        this.quantity = orderItem.getQuantity();
        this.totalPrice = orderItem.getPriceAtOrder() * orderItem.getQuantity();
    }

    // Getters and Setters
    public Long getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(Long orderItemId) {
        this.orderItemId = orderItemId;
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

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Double getPriceAtOrder() {
        return priceAtOrder;
    }

    public void setPriceAtOrder(Double priceAtOrder) {
        this.priceAtOrder = priceAtOrder;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }
}