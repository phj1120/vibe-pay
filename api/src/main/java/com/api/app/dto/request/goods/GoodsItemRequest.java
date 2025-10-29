package com.api.app.dto.request.goods;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;

/**
 * 상품 단품 등록/수정 요청 DTO
 *
 * @author system
 * @version 1.0
 * @since 2025-10-29
 */
@Alias("GoodsItemRequest")
@Getter
@Setter
public class GoodsItemRequest implements Serializable {
    private static final long serialVersionUID = 5678901234567890123L;

    @Schema(description = "단품명", example = "블랙")
    @NotBlank(message = "단품명은 필수입니다")
    private String itemName;

    @Schema(description = "단품금액", example = "5000")
    @NotNull(message = "단품금액은 필수입니다")
    @Min(value = 0, message = "단품금액은 0 이상이어야 합니다")
    private Long itemPrice;

    @Schema(description = "재고수량", example = "100")
    @NotNull(message = "재고수량은 필수입니다")
    @Min(value = 0, message = "재고수량은 0 이상이어야 합니다")
    private Long stock;

    @Schema(description = "단품상태코드", example = "001")
    @NotBlank(message = "단품상태코드는 필수입니다")
    private String goodsStatusCode;
}
