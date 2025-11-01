package com.api.app.dto.response.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 주문 완료 응답 DTO
 *
 * @author Claude
 * @version 1.0
 * @since 2025-11-01
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "주문 완료 응답")
public class OrderCompleteResponse {

    @Schema(description = "주문번호")
    private String orderNo;

    @Schema(description = "회원번호")
    private String memberNo;

    @Schema(description = "주문접수일시")
    private LocalDateTime orderAcceptDtm;

    @Schema(description = "총 주문금액")
    private Long totalAmount;

    @Schema(description = "주문상품 목록")
    private List<OrderCompleteGoods> goodsList;

    @Schema(description = "결제정보 목록")
    private List<OrderCompletePayment> paymentList;

    /**
     * 주문 완료 상품 정보
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "주문 완료 상품 정보")
    public static class OrderCompleteGoods {

        @Schema(description = "상품번호")
        private String goodsNo;

        @Schema(description = "단품번호")
        private String itemNo;

        @Schema(description = "상품명")
        private String goodsName;

        @Schema(description = "단품명")
        private String itemName;

        @Schema(description = "판매가")
        private Long salePrice;

        @Schema(description = "수량")
        private Long quantity;

        @Schema(description = "소계")
        private Long subtotal;
    }

    /**
     * 주문 완료 결제 정보
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "주문 완료 결제 정보")
    public static class OrderCompletePayment {

        @Schema(description = "결제방식코드")
        private String payWayCode;

        @Schema(description = "결제방식명")
        private String payWayName;

        @Schema(description = "결제금액")
        private Long amount;

        @Schema(description = "PG사코드")
        private String pgTypeCode;

        @Schema(description = "PG사명")
        private String pgTypeName;
    }
}
