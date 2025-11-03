package com.api.app.dto.response.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 취소 가능한 주문 조회 응답 DTO
 *
 * @author Claude
 * @version 1.0
 * @since 2025-11-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelableOrderResponse {

    /**
     * 주문번호
     */
    private String orderNo;

    /**
     * 취소 가능한 주문 순번 목록
     */
    private List<CancelableOrderItem> cancelableItems;

    /**
     * 환불 정보
     */
    private RefundInfo refundInfo;

    /**
     * 취소 가능한 주문 상품 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CancelableOrderItem {

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
         * 소계 (판매가격 × 수량)
         */
        private Long subtotal;
    }

    /**
     * 환불 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefundInfo {

        /**
         * 환불 예정 총액
         */
        private Long totalRefundAmount;

        /**
         * 결제 수단별 환불 상세
         */
        private List<RefundDetail> refundDetails;
    }

    /**
     * 결제 수단별 환불 상세
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefundDetail {

        /**
         * 결제방식코드
         */
        private String payWayCode;

        /**
         * 결제방식명
         */
        private String payWayName;

        /**
         * 환불금액
         */
        private Long refundAmount;

        /**
         * PG사코드 (카드 결제인 경우)
         */
        private String pgTypeCode;

        /**
         * PG사명 (카드 결제인 경우)
         */
        private String pgTypeName;
    }
}
