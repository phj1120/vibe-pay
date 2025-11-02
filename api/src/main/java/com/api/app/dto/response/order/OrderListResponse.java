package com.api.app.dto.response.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 주문 목록 응답 DTO
 *
 * @author Claude
 * @version 1.0
 * @since 2025-11-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderListResponse {

    /**
     * 주문번호
     */
    private String orderNo;

    /**
     * 주문일시
     */
    private LocalDateTime orderAcceptDtm;

    /**
     * 총 주문금액
     */
    private Long totalAmount;

    /**
     * 주문 상품 목록
     */
    private List<OrderListGoods> goodsList;

    /**
     * 주문 상품 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderListGoods {

        /**
         * 주문순번
         */
        private Long orderSequence;

        /**
         * 주문처리순번
         */
        private Long orderProcessSequence;

        /**
         * 상품번호
         */
        private String goodsNo;

        /**
         * 단품번호
         */
        private String itemNo;

        /**
         * 상품명
         */
        private String goodsName;

        /**
         * 단품명
         */
        private String itemName;

        /**
         * 판매가격
         */
        private Long salePrice;

        /**
         * 수량
         */
        private Long quantity;

        /**
         * 주문상태코드
         */
        private String orderStatusCode;

        /**
         * 주문상태명
         */
        private String orderStatusName;

        /**
         * 주문유형코드
         */
        private String orderTypeCode;

        /**
         * 주문유형명
         */
        private String orderTypeName;

        /**
         * 취소가능여부
         */
        private Boolean cancelable;

        /**
         * 취소가능금액
         */
        private Long cancelableAmount;
    }
}
