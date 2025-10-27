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
public class OrderItemRequest {
    private Long productId;
    private Integer quantity;
}
