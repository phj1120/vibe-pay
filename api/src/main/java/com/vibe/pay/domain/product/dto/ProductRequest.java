package com.vibe.pay.domain.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.math.BigDecimal;

/**
 * 상품 요청 DTO
 * 상품 생성 및 수정 요청 시 사용
 *
 * @author claude
 * @version 1.0
 * @since 2025-10-16
 */
@Alias("ProductRequest")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    private static final long serialVersionUID = 3333333333333333333L;

    /**
     * 상품명
     */
    @Schema(description = "상품명", example = "노트북")
    // @NotBlank(message = "상품명은 필수입니다")
    private String name;

    /**
     * 상품 가격
     */
    @Schema(description = "상품 가격", example = "1500000")
    // @NotNull(message = "가격은 필수입니다")
    // @Min(value = 0, message = "가격은 0 이상이어야 합니다")
    private BigDecimal price;
}
