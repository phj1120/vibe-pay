package com.api.app.mapper.pointhistory;

import com.api.app.entity.pointhistory.PointHistoryDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PointHistoryMapper {
    void insertPointHistory(PointHistoryDto pointHistoryDto);
    List<PointHistoryDto> selectPointHistoryList();
    PointHistoryDto selectPointHistory(String pointHistoryNo);
    List<PointHistoryDto> findAvailablePointsByMemberNo(String memberNo);
    Long findTotalAvailablePointsByMemberNo(String memberNo);
    void updatePointHistory(PointHistoryDto pointHistoryDto);
    void deletePointHistory(String pointHistoryNo);
}
