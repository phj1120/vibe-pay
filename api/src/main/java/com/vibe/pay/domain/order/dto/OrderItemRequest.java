package com.vibe.pay.domain.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

/**
 * 주문 상품 요청 DTO
 * 주문 생성 시 포함되는 개별 상품 정보
 *
 * @author claude
 * @version 1.0
 * @since 2025-10-16
 */
@Alias("OrderItemRequest")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequest {
    private static final long serialVersionUID = 5555555555555555555L;

    /**
     * 상품 ID
     */
    @Schema(description = "상품 ID", example = "1")
    // @NotNull(message = "상품 ID는 필수입니다")
    private Long productId;

    /**
     * 주문 수량
     */
    @Schema(description = "주문 수량", example = "2")
    // @NotNull(message = "주문 수량은 필수입니다")
    // @Min(value = 1, message = "주문 수량은 1 이상이어야 합니다")
    private Integer quantity;
}
