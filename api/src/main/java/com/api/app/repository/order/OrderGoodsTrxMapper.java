package com.api.app.repository.order;

import com.api.app.entity.OrderGoods;
import org.apache.ibatis.annotations.Param;

/**
 * @author system
 * @version 1.0
 * @since 2025-10-27
 */
public interface OrderGoodsTrxMapper {

    /**
     * 주문상품 등록
     *
     * @param orderGoods 주문상품 정보
     * @return 등록된 건수
     */
    int insertOrderGoods(OrderGoods orderGoods);

    /**
     * 주문상품 수정
     *
     * @param orderGoods 주문상품 정보
     * @return 수정된 건수
     */
    int updateOrderGoods(OrderGoods orderGoods);

    /**
     * 주문상품 삭제 (복합키)
     *
     * @param orderNo 주문번호
     * @param goodsNo 상품번호
     * @param itemNo  단품번호
     * @return 삭제된 건수
     */
    int deleteOrderGoods(
            @Param("orderNo") String orderNo,
            @Param("goodsNo") String goodsNo,
            @Param("itemNo") String itemNo
    );
}
