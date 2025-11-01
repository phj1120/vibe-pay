package com.api.app.repository.order;

import com.api.app.dto.response.order.OrderCompleteResponse;
import com.api.app.entity.OrderBase;
import org.apache.ibatis.annotations.Param;

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

    /**
     * 주문 완료 정보 조회
     *
     * @param orderNo 주문번호
     * @param memberNo 회원번호
     * @return 주문 완료 정보
     */
    OrderCompleteResponse selectOrderCompleteByOrderNo(@Param("orderNo") String orderNo, @Param("memberNo") String memberNo);
}
