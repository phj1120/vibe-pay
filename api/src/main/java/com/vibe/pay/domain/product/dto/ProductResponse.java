package com.vibe.pay.domain.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.math.BigDecimal;

/**
 * 상품 응답 DTO
 * 상품 조회 결과 반환 시 사용
 *
 * @author claude
 * @version 1.0
 * @since 2025-10-16
 */
@Alias("ProductResponse")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private static final long serialVersionUID = 4444444444444444444L;

    /**
     * 상품 ID (Primary Key)
     */
    @Schema(description = "상품 ID")
    private Long productId;

    /**
     * 상품명
     */
    @Schema(description = "상품명")
    private String name;

    /**
     * 상품 가격
     */
    @Schema(description = "상품 가격")
    private BigDecimal price;
}
