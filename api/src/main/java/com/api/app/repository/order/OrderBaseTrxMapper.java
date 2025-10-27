package com.api.app.repository.order;

import com.api.app.entity.OrderBase;

/**
 * @author system
 * @version 1.0
 * @since 2025-10-27
 */
public interface OrderBaseTrxMapper {

    /**
     * 주문번호 시퀀스 생성 (날짜+O+시퀀스)
     *
     * @return 생성된 주문번호 (ex. 20251027O000001)
     */
    String generateOrderNo();

    /**
     * 클레임번호 시퀀스 생성 (날짜+C+시퀀스)
     *
     * @return 생성된 클레임번호 (ex. 20251027C000001)
     */
    String generateClaimNo();

    /**
     * 주문 등록
     *
     * @param orderBase 주문 정보
     * @return 등록된 건수
     */
    int insertOrderBase(OrderBase orderBase);

    /**
     * 주문 수정
     *
     * @param orderBase 주문 정보
     * @return 수정된 건수
     */
    int updateOrderBase(OrderBase orderBase);

    /**
     * 주문 삭제
     *
     * @param orderNo 주문번호
     * @return 삭제된 건수
     */
    int deleteOrderBase(String orderNo);
}
