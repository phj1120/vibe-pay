package com.vibe.pay.domain.pointhistory.repository;

import com.vibe.pay.domain.pointhistory.entity.PointHistory;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Optional;

@Mapper
public interface PointHistoryMapper {
    void insert(PointHistory pointHistory);
    Optional<PointHistory> findByPointHistoryId(Long pointHistoryId);
    List<PointHistory> findByMemberId(Long memberId);
    List<PointHistory> findAll();
    void delete(Long pointHistoryId);
}
