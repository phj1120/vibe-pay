package com.api.app.dto.response.order;

import com.api.app.dto.response.basket.BasketResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.util.List;

/**
 * 주문서 조회 응답 DTO
 *
 * @author system
 * @version 1.0
 * @since 2025-10-30
 */
@Alias("OrderSheetResponse")
@Getter
@Setter
public class OrderSheetResponse implements Serializable {
    private static final long serialVersionUID = 2025103012345678901L;

    @Schema(description = "주문 상품 목록")
    private List<BasketResponse> items;

    @Schema(description = "주문자 이름")
    private String ordererName;

    @Schema(description = "주문자 이메일")
    private String ordererEmail;

    @Schema(description = "주문자 연락처")
    private String ordererPhone;

    @Schema(description = "총 상품 금액")
    private Long totalProductAmount;

    @Schema(description = "총 수량")
    private Long totalQuantity;
}
