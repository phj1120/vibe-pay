package com.api.app.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;

/**
 * @author system
 * @version 1.0
 * @since 2025-10-27
 */
@Alias("BasketBase")
@Getter
@Setter
public class BasketBase implements Serializable {
    private static final long serialVersionUID = 1234567890123456789L;

    @Schema(description = "장바구니번호")
    private String basketNo;

    @Schema(description = "회원번호")
    private String memberNo;

    @Schema(description = "상품번호")
    private String goodsNo;

    @Schema(description = "단품번호")
    private String itemNo;

    @Schema(description = "수량")
    private Long quantity;

    @Schema(description = "주문여부")
    private Boolean isOrder;
}
