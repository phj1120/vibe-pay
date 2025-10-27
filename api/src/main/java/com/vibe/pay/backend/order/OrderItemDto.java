package com.vibe.pay.backend.order;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
@Getter
@Setter
public class OrderItemDto {
    private Long orderItemId;
    private String orderId;
    private Integer ordSeq;
    private Integer ordProcSeq;
    private Long productId;
    private String productName;
    private Integer quantity;
    private Double priceAtOrder;
}
