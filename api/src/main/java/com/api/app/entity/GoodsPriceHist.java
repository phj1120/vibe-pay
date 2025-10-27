package com.api.app.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author system
 * @version 1.0
 * @since 2025-10-27
 */
@Alias("GoodsPriceHist")
@Getter
@Setter
public class GoodsPriceHist implements Serializable {
    private static final long serialVersionUID = 4456789012345678901L;

    @Schema(description = "상품번호")
    private String goodsNo;

    @Schema(description = "시작일")
    private LocalDateTime startDateTime;

    @Schema(description = "종료일")
    private LocalDateTime endDateTime;

    @Schema(description = "판매가")
    private Long salePrice;

    @Schema(description = "공급원가")
    private Long supplyPrice;
}
