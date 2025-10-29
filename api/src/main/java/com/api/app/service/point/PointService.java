package com.api.app.service.point;

import com.api.app.dto.request.point.PointHistoryRequest;
import com.api.app.dto.request.point.PointTransactionRequest;
import com.api.app.dto.response.point.PointBalanceResponse;
import com.api.app.dto.response.point.PointHistoryListResponse;

/**
 * 포인트 서비스 인터페이스
 *
 * @author system
 * @version 1.0
 * @since 2025-10-29
 */
public interface PointService {

    /**
     * 포인트 충전/사용 처리
     *
     * @param email 이메일 (회원 식별)
     * @param request 포인트 거래 요청
     */
    void processPointTransaction(String email, PointTransactionRequest request);

    /**
     * 보유 포인트 조회
     *
     * @param email 이메일 (회원 식별)
     * @return 보유 포인트
     */
    PointBalanceResponse getPointBalance(String email);

    /**
     * 포인트 내역 목록 조회
     *
     * @param email 이메일 (회원 식별)
     * @param request 조회 조건
     * @return 포인트 내역 목록
     */
    PointHistoryListResponse getPointHistoryList(String email, PointHistoryRequest request);
}
