package com.api.app.repository.order;

import com.api.app.entity.OrderDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author system
 * @version 1.0
 * @since 2025-10-27
 */
public interface OrderDetailMapper {

    /**
     * 주문상세 전체 조회
     *
     * @return 주문상세 목록
     */
    List<OrderDetail> selectAllOrderDetail();

    /**
     * 주문번호로 조회
     *
     * @param orderNo 주문번호
     * @return 주문상세 목록
     */
    List<OrderDetail> selectOrderDetailByOrderNo(String orderNo);

    /**
     * 주문번호와 주문순번으로 조회
     *
     * @param orderNo       주문번호
     * @param orderSequence 주문순번
     * @return 주문상세 목록
     */
    List<OrderDetail> selectOrderDetailByOrderNoAndSequence(
            @Param("orderNo") String orderNo,
            @Param("orderSequence") Long orderSequence
    );

    /**
     * 복합키로 단건 조회
     *
     * @param orderNo              주문번호
     * @param orderSequence        주문순번
     * @param orderProcessSequence 주문처리순번
     * @return 주문상세 정보
     */
    OrderDetail selectOrderDetailByPk(
            @Param("orderNo") String orderNo,
            @Param("orderSequence") Long orderSequence,
            @Param("orderProcessSequence") Long orderProcessSequence
    );
}
