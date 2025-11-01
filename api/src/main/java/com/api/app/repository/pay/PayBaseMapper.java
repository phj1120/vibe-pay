package com.api.app.repository.pay;

import com.api.app.dto.response.order.OrderCompleteResponse;
import com.api.app.entity.PayBase;

import java.util.List;

/**
 * @author system
 * @version 1.0
 * @since 2025-10-27
 */
public interface PayBaseMapper {

    /**
     * 결제 전체 조회
     *
     * @return 결제 목록
     */
    List<PayBase> selectAllPayBase();

    /**
     * 결제번호로 조회
     *
     * @param payNo 결제번호
     * @return 결제 정보
     */
    PayBase selectPayBaseByPayNo(String payNo);

    /**
     * 주문번호로 조회
     *
     * @param orderNo 주문번호
     * @return 결제 목록
     */
    List<PayBase> selectPayBaseByOrderNo(String orderNo);

    /**
     * 회원번호로 조회
     *
     * @param memberNo 회원번호
     * @return 결제 목록
     */
    List<PayBase> selectPayBaseByMemberNo(String memberNo);

    /**
     * 승인번호로 조회
     *
     * @param approveNo 승인번호
     * @return 결제 정보
     */
    PayBase selectPayBaseByApproveNo(String approveNo);

    /**
     * 주문 완료 결제 정보 목록 조회
     *
     * @param orderNo 주문번호
     * @return 주문 완료 결제 정보 목록
     */
    List<OrderCompleteResponse.OrderCompletePayment> selectOrderCompletePaymentByOrderNo(String orderNo);
}
