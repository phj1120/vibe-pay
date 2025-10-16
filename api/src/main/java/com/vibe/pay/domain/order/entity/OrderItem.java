package com.vibe.pay.domain.order.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.math.BigDecimal;

/**
 * 주문 상품 엔티티
 * 주문에 포함된 개별 상품 정보를 관리하는 엔티티 클래스
 */
@Alias("OrderItem")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    private static final long serialVersionUID = 4567890123456789012L;

    /**
     * 주문 상품 ID (Primary Key)
     */
    private Long orderItemId;

    /**
     * 주문 ID (Foreign Key)
     */
    private String orderId;

    /**
     * 주문 순번 (Foreign Key)
     */
    private Integer ordSeq;

    /**
     * 주문 처리 순번 (Foreign Key)
     */
    private Integer ordProcSeq;

    /**
     * 상품 ID (Foreign Key)
     */
    private Long productId;

    /**
     * 주문 수량
     */
    private Integer quantity;

    /**
     * 주문 시점의 상품 가격
     */
    private BigDecimal priceAtOrder;
}
