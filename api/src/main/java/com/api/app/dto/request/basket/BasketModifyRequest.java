package com.api.app.dto.request.basket;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;

/**
 * 장바구니 수정 요청 DTO
 *
 * @author system
 * @version 1.0
 * @since 2025-10-29
 */
@Alias("BasketModifyRequest")
@Getter
@Setter
public class BasketModifyRequest implements Serializable {
    private static final long serialVersionUID = 1234567890123456002L;

    @Schema(description = "상품번호", example = "G00000000000001")
    private String goodsNo;

    @Schema(description = "단품번호", example = "001")
    private String itemNo;

    @Schema(description = "수량", example = "2")
    @Min(value = 1, message = "수량은 1 이상이어야 합니다")
    private Long quantity;
}
