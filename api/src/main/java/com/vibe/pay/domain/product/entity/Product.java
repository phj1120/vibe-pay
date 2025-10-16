package com.vibe.pay.domain.product.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.math.BigDecimal;

/**
 * 상품 엔티티
 * 상품 정보를 관리하는 엔티티 클래스
 */
@Alias("Product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private static final long serialVersionUID = 2345678901234567890L;

    /**
     * 상품 ID (Primary Key)
     */
    private Long productId;

    /**
     * 상품명
     */
    private String name;

    /**
     * 상품 가격
     */
    private BigDecimal price;
}
