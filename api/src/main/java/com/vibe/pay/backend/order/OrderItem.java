package com.vibe.pay.backend.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    private Long orderItemId;
    private String orderId;
    private Integer ordSeq;
    private Integer ordProcSeq;
    private Long productId;
    private Integer quantity;
    private Double priceAtOrder;
}
