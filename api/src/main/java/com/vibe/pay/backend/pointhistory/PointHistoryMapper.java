package com.vibe.pay.backend.pointhistory;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
@Mapper
public interface PointHistoryMapper {
    void insert(PointHistory pointHistory);

    PointHistory selectById(Long pointHistoryId);

    List<PointHistory> selectByMemberId(Long memberId);

    List<PointHistory> selectAll();

    void delete(Long pointHistoryId);
}
