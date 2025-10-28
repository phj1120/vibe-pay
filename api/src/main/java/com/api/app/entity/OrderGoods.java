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
@Alias("OrderGoods")
@Getter
@Setter
public class OrderGoods extends SystemEntity {
    private static final long serialVersionUID = 4567890123456789012L;

    @Schema(description = "주문번호")
    private String orderNo;

    @Schema(description = "상품번호")
    private String goodsNo;

    @Schema(description = "단품번호")
    private String itemNo;

    @Schema(description = "판매가")
    private Long salePrice;

    @Schema(description = "공급원가")
    private Long supplyPrice;

    @Schema(description = "상품명")
    private String goodsName;

    @Schema(description = "단품명")
    private String itemName;
}
