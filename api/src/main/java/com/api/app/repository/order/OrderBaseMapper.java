package com.api.app.repository.order;

import com.api.app.entity.OrderBase;

import java.util.List;

/**
 * @author system
 * @version 1.0
 * @since 2025-10-27
 */
public interface OrderBaseMapper {

    /**
     * 주문 전체 조회
     *
     * @return 주문 목록
     */
    List<OrderBase> selectAllOrderBase();

    /**
     * 주문번호로 조회
     *
     * @param orderNo 주문번호
     * @return 주문 정보
     */
    OrderBase selectOrderBaseByOrderNo(String orderNo);

    /**
     * 회원번호로 조회
     *
     * @param memberNo 회원번호
     * @return 주문 목록
     */
    List<OrderBase> selectOrderBaseByMemberNo(String memberNo);
}
