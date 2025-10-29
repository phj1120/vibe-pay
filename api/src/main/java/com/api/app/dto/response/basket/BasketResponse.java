package com.api.app.dto.response.basket;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 장바구니 조회 응답 DTO
 *
 * @author system
 * @version 1.0
 * @since 2025-10-29
 */
@Alias("BasketResponse")
@Getter
@Setter
public class BasketResponse implements Serializable {
    private static final long serialVersionUID = 1234567890123456003L;

    @Schema(description = "장바구니번호")
    private String basketNo;

    @Schema(description = "회원번호")
    private String memberNo;

    @Schema(description = "상품번호")
    private String goodsNo;

    @Schema(description = "상품명")
    private String goodsName;

    @Schema(description = "상품상태코드")
    private String goodsStatusCode;

    @Schema(description = "상품대표이미지주소")
    private String goodsMainImageUrl;

    @Schema(description = "단품번호")
    private String itemNo;

    @Schema(description = "단품명")
    private String itemName;

    @Schema(description = "단품금액")
    private Long itemPrice;

    @Schema(description = "단품상태코드")
    private String itemStatusCode;

    @Schema(description = "재고수량")
    private Long stock;

    @Schema(description = "수량")
    private Long quantity;

    @Schema(description = "주문여부")
    private Boolean isOrder;

    @Schema(description = "등록일시")
    private LocalDateTime registDateTime;
}
