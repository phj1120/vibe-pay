package com.api.app.repository.order;

import com.api.app.dto.response.order.OrderCompleteResponse;
import com.api.app.entity.OrderGoods;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author system
 * @version 1.0
 * @since 2025-10-27
 */
public interface OrderGoodsMapper {

    /**
     * 주문상품 전체 조회
     *
     * @return 주문상품 목록
     */
    List<OrderGoods> selectAllOrderGoods();

    /**
     * 주문번호로 조회
     *
     * @param orderNo 주문번호
     * @return 주문상품 목록
     */
    List<OrderGoods> selectOrderGoodsByOrderNo(String orderNo);

    /**
     * 복합키로 단건 조회
     *
     * @param orderNo 주문번호
     * @param goodsNo 상품번호
     * @param itemNo  단품번호
     * @return 주문상품 정보
     */
    OrderGoods selectOrderGoodsByPk(
            @Param("orderNo") String orderNo,
            @Param("goodsNo") String goodsNo,
            @Param("itemNo") String itemNo
    );

    /**
     * 주문 완료 상품 목록 조회
     *
     * @param orderNo 주문번호
     * @return 주문 완료 상품 목록
     */
    List<OrderCompleteResponse.OrderCompleteGoods> selectOrderCompleteGoodsByOrderNo(String orderNo);

    /**
     * 복합키로 단건 조회 (별칭)
     *
     * @param orderNo 주문번호
     * @param goodsNo 상품번호
     * @param itemNo  단품번호
     * @return 주문상품 정보
     */
    default OrderGoods selectOrderGoodsByKey(String orderNo, String goodsNo, String itemNo) {
        return selectOrderGoodsByPk(orderNo, goodsNo, itemNo);
    }
}
