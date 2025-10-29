package com.api.app.repository.point;

import com.api.app.dto.request.point.PointHistoryRequest;
import com.api.app.dto.response.point.PointBalanceResponse;
import com.api.app.dto.response.point.PointHistoryResponse;
import com.api.app.entity.PointHistory;

import java.util.List;

/**
 * 포인트 내역 조회용 Mapper
 *
 * @author system
 * @version 1.0
 * @since 2025-10-29
 */
public interface PointHistoryMapper {

    /**
     * 회원의 보유 포인트 조회
     *
     * @param memberNo 회원번호
     * @return 보유 포인트
     */
    PointBalanceResponse selectPointBalance(String memberNo);

    /**
     * 회원의 포인트 내역 목록 조회
     *
     * @param request 조회 조건
     * @return 포인트 내역 목록
     */
    List<PointHistoryResponse> selectPointHistoryList(PointHistoryRequest request);

    /**
     * 회원의 포인트 내역 총 개수 조회
     *
     * @param request 조회 조건
     * @return 총 개수
     */
    Long countPointHistory(PointHistoryRequest request);

    /**
     * 사용 가능한 포인트 내역 조회 (종료일시 기준 정렬)
     *
     * @param memberNo 회원번호
     * @return 사용 가능한 포인트 내역 목록
     */
    List<PointHistory> selectAvailablePointHistory(String memberNo);
}
