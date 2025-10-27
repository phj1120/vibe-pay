package com.api.app.repository.order;

import com.api.app.entity.OrderDetail;
import org.apache.ibatis.annotations.Param;

/**
 * @author system
 * @version 1.0
 * @since 2025-10-27
 */
public interface OrderDetailTrxMapper {

    /**
     * 주문상세 등록
     *
     * @param orderDetail 주문상세 정보
     * @return 등록된 건수
     */
    int insertOrderDetail(OrderDetail orderDetail);

    /**
     * 주문상세 수정
     *
     * @param orderDetail 주문상세 정보
     * @return 수정된 건수
     */
    int updateOrderDetail(OrderDetail orderDetail);

    /**
     * 주문상세 삭제 (복합키)
     *
     * @param orderNo              주문번호
     * @param orderSequence        주문순번
     * @param orderProcessSequence 주문처리순번
     * @return 삭제된 건수
     */
    int deleteOrderDetail(
            @Param("orderNo") String orderNo,
            @Param("orderSequence") Long orderSequence,
            @Param("orderProcessSequence") Long orderProcessSequence
    );
}
