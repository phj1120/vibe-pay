package com.api.app.service.order;

import com.api.app.dto.response.order.OrderSheetResponse;

import java.util.List;

/**
 * 주문서 서비스 인터페이스
 *
 * @author system
 * @version 1.0
 * @since 2025-10-30
 */
public interface OrderSheetService {

    /**
     * 주문서 정보 조회
     *
     * @param email     회원 이메일
     * @param basketNos 장바구니 번호 목록
     * @return 주문서 정보
     */
    OrderSheetResponse getOrderSheet(String email, List<String> basketNos);
}
