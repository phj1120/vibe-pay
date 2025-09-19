package com.vibe.pay.backend.order;

public class OrderItem {

    private Long orderItemId;
    private String orderId;
    private Integer ordSeq;
    private Integer ordProcSeq;
    private Long productId;
    private Integer quantity;
    private Double priceAtOrder;

    public OrderItem() {
    }

    public OrderItem(String orderId, Integer ordSeq, Integer ordProcSeq, Long productId, Integer quantity, Double priceAtOrder) {
        this.orderId = orderId;
        this.ordSeq = ordSeq;
        this.ordProcSeq = ordProcSeq;
        this.productId = productId;
        this.quantity = quantity;
        this.priceAtOrder = priceAtOrder;
    }

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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getPriceAtOrder() {
        return priceAtOrder;
    }

    public void setPriceAtOrder(Double priceAtOrder) {
        this.priceAtOrder = priceAtOrder;
    }
}
