package com.api.app.dto.response.goods;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;

/**
 * 상품 단품 응답 DTO
 *
 * @author system
 * @version 1.0
 * @since 2025-10-29
 */
@Alias("GoodsItemResponse")
@Getter
@Setter
public class GoodsItemResponse implements Serializable {
    private static final long serialVersionUID = 9012345678901234567L;

    @Schema(description = "상품번호")
    private String goodsNo;

    @Schema(description = "단품번호")
    private String itemNo;

    @Schema(description = "단품명")
    private String itemName;

    @Schema(description = "단품금액")
    private Long itemPrice;

    @Schema(description = "재고수량")
    private Long stock;

    @Schema(description = "단품상태코드")
    private String goodsStatusCode;

    @Schema(description = "단품상태명")
    private String goodsStatusName;

    @Schema(description = "품절여부")
    private Boolean isSoldOut;
}
