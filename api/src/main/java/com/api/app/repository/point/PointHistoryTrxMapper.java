package com.api.app.repository.point;

import com.api.app.entity.PointHistory;

/**
 * 포인트 내역 등록/수정/삭제용 Mapper
 *
 * @author system
 * @version 1.0
 * @since 2025-10-29
 */
public interface PointHistoryTrxMapper {

    /**
     * 포인트 내역 등록
     *
     * @param pointHistory 포인트 내역
     * @return 등록된 행 수
     */
    int insertPointHistory(PointHistory pointHistory);

    /**
     * 포인트 잔여 포인트 수정
     *
     * @param pointHistory 포인트 내역
     * @return 수정된 행 수
     */
    int updateRemainPoint(PointHistory pointHistory);

    /**
     * 포인트 기록 번호 생성
     *
     * @return 생성된 포인트 기록 번호
     */
    String generatePointHistoryNo();
}
