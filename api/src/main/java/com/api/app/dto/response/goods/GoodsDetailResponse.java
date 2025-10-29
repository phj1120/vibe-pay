package com.api.app.dto.response.goods;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 상품 상세 응답 DTO
 *
 * @author system
 * @version 1.0
 * @since 2025-10-29
 */
@Alias("GoodsDetailResponse")
@Getter
@Setter
public class GoodsDetailResponse implements Serializable {
    private static final long serialVersionUID = 2233445566778899001L;

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

    @Schema(description = "단품 목록")
    private List<GoodsItemResponse> items;

    @Schema(description = "등록일시")
    private LocalDateTime registDateTime;

    @Schema(description = "수정일시")
    private LocalDateTime modifyDateTime;
}
