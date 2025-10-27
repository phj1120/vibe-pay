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
@Alias("OrderDetail")
@Getter
@Setter
public class OrderDetail implements Serializable {
    private static final long serialVersionUID = 3456789012345678901L;

    @Schema(description = "주문번호")
    private String orderNo;

    @Schema(description = "주문순번")
    private Long orderSequence;

    @Schema(description = "주문처리순번")
    private Long orderProcessSequence;

    @Schema(description = "상위주문처리순번")
    private Long upperOrderProcessSequence;

    @Schema(description = "클레임번호")
    private String claimNo;

    @Schema(description = "상품번호")
    private String goodsNo;

    @Schema(description = "단품번호")
    private String itemNo;

    @Schema(description = "수량")
    private Long quantity;

    @Schema(description = "주문상태코드")
    private String orderStatusCode;

    @Schema(description = "배송구분코드")
    private String deliveryTypeCode;

    @Schema(description = "주문유형코드")
    private String orderTypeCode;

    @Schema(description = "주문접수일시")
    private LocalDateTime orderAcceptDtm;

    @Schema(description = "주문완료일시")
    private LocalDateTime orderFinishDtm;
}
