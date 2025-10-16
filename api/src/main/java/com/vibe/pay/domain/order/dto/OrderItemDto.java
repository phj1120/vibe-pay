package com.vibe.pay.domain.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.math.BigDecimal;

/**
 * 주문 상품 DTO
 * 주문 상세 조회 시 포함되는 개별 상품 정보
 *
 * @author claude
 * @version 1.0
 * @since 2025-10-16
 */
@Alias("OrderItemDto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {
    private static final long serialVersionUID = 999999999999999999L;

    /**
     * 주문 상품 ID (Primary Key)
     */
    @Schema(description = "주문 상품 ID")
    private Long orderItemId;

    /**
     * 주문 ID (Foreign Key)
     */
    @Schema(description = "주문 ID")
    private String orderId;

    /**
     * 주문 순번 (Foreign Key)
     */
    @Schema(description = "주문 순번")
    private Integer ordSeq;

    /**
     * 주문 처리 순번 (Foreign Key)
     */
    @Schema(description = "주문 처리 순번")
    private Integer ordProcSeq;

    /**
     * 상품 ID (Foreign Key)
     */
    @Schema(description = "상품 ID")
    private Long productId;

    /**
     * 상품명
     */
    @Schema(description = "상품명")
    private String productName;

    /**
     * 주문 시점의 상품 가격
     */
    @Schema(description = "주문 시점의 상품 가격")
    private BigDecimal priceAtOrder;

    /**
     * 주문 수량
     */
    @Schema(description = "주문 수량")
    private Integer quantity;

    /**
     * 총 가격 (priceAtOrder * quantity)
     */
    @Schema(description = "총 가격")
    private BigDecimal totalPrice;
}
