package com.vibe.pay.backend.order;

public class OrderItem {

    private Long id;
    private String orderId;
    private Long productId;
    private Integer quantity;
    private Double priceAtOrder;

    public OrderItem() {
    }

    public OrderItem(String orderId, Long productId, Integer quantity, Double priceAtOrder) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.priceAtOrder = priceAtOrder;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
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
