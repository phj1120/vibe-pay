package com.api.app.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

/**
 * @author system
 * @version 1.0
 * @since 2025-10-27
 */
@Alias("GoodsItem")
@Getter
@Setter
public class GoodsItem extends SystemEntity {
    private static final long serialVersionUID = 3345678901234567890L;

    @Schema(description = "상품번호")
    private String goodsNo;

    @Schema(description = "단품번호")
    private String itemNo;

    @Schema(description = "상품명")
    private String goodsName;

    @Schema(description = "단품금액")
    private Long itemPrice;

    @Schema(description = "재고수량")
    private Long stock;

    @Schema(description = "단품상태코드")
    private String goodsStatusCode;
}
