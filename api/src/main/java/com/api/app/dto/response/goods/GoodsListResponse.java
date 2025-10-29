package com.api.app.dto.response.goods;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 상품 목록 응답 DTO
 *
 * @author system
 * @version 1.0
 * @since 2025-10-29
 */
@Alias("GoodsListResponse")
@Getter
@Setter
public class GoodsListResponse implements Serializable {
    private static final long serialVersionUID = 1122334455667788990L;

    @Schema(description = "상품번호")
    private String goodsNo;

    @Schema(description = "상품명")
    private String goodsName;

    @Schema(description = "상품상태코드")
    private String goodsStatusCode;

    @Schema(description = "상품상태명")
    private String goodsStatusName;

    @Schema(description = "상품대표이미지주소")
    private String goodsMainImageUrl;

    @Schema(description = "판매가")
    private Long salePrice;

    @Schema(description = "공급원가")
    private Long supplyPrice;

    @Schema(description = "최소 단품 가격")
    private Long minItemPrice;

    @Schema(description = "최대 단품 가격")
    private Long maxItemPrice;

    @Schema(description = "전체 재고수량")
    private Long totalStock;

    @Schema(description = "판매가능여부")
    private Boolean isAvailable;

    @Schema(description = "등록일시")
    private LocalDateTime registDateTime;
}
