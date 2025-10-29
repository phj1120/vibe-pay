package com.api.app.dto.request.goods;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.util.List;

/**
 * 상품 등록 요청 DTO
 *
 * @author system
 * @version 1.0
 * @since 2025-10-29
 */
@Alias("GoodsRegisterRequest")
@Getter
@Setter
public class GoodsRegisterRequest implements Serializable {
    private static final long serialVersionUID = 6789012345678901234L;

    @Schema(description = "상품명", example = "맛있는 사과")
    @NotBlank(message = "상품명은 필수입니다")
    private String goodsName;

    @Schema(description = "상품상태코드", example = "001")
    @NotBlank(message = "상품상태코드는 필수입니다")
    private String goodsStatusCode;

    @Schema(description = "상품대표이미지주소", example = "https://example.com/apple.jpg")
    @NotBlank(message = "상품대표이미지주소는 필수입니다")
    private String goodsMainImageUrl;

    @Schema(description = "판매가", example = "10000")
    @NotNull(message = "판매가는 필수입니다")
    @Min(value = 0, message = "판매가는 0 이상이어야 합니다")
    private Long salePrice;

    @Schema(description = "공급원가", example = "7000")
    @NotNull(message = "공급원가는 필수입니다")
    @Min(value = 0, message = "공급원가는 0 이상이어야 합니다")
    private Long supplyPrice;

    @Schema(description = "단품 목록")
    @NotEmpty(message = "단품은 최소 1개 이상 등록해야 합니다")
    @Valid
    private List<GoodsItemRequest> items;
}
