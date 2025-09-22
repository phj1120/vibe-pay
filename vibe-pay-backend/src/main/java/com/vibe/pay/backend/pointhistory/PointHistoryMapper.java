package com.vibe.pay.backend.pointhistory;

import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface PointHistoryMapper {

    void insert(PointHistory pointHistory);

    PointHistory findByPointHistoryId(@Param("pointHistoryId") Long pointHistoryId);

    List<PointHistory> findByMemberId(@Param("memberId") Long memberId);
    
    List<PointHistory> findByMemberIdWithPaging(@Param("memberId") Long memberId, @Param("offset") int offset, @Param("limit") int limit);

    List<PointHistory> findByMemberIdAndReferenceId(@Param("memberId") Long memberId, @Param("referenceId") String referenceId);

    List<PointHistory> findByReferenceTypeAndId(@Param("referenceType") String referenceType, @Param("referenceId") String referenceId);

    List<PointHistory> findByMemberIdAndTransactionType(@Param("memberId") Long memberId, @Param("transactionType") String transactionType);

    List<PointHistory> findAll();

    void update(PointHistory pointHistory);

    void delete(@Param("pointHistoryId") Long pointHistoryId);

    // 포인트 히스토리 ID 시퀀스 조회 (필요 시)
    Long getNextPointHistorySequence();
}