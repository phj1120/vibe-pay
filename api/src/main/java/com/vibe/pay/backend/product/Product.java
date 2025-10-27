package com.vibe.pay.backend.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private Long productId;
    private String name;
    private Double price;
}
