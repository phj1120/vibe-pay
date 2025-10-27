package com.vibe.pay.backend.rewardpoints;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
@Mapper
public interface RewardPointsMapper {
    void insert(RewardPoints rewardPoints);

    RewardPoints selectById(Long rewardPointsId);

    RewardPoints selectByMemberId(Long memberId);

    List<RewardPoints> selectAll();

    void update(RewardPoints rewardPoints);

    void delete(Long rewardPointsId);
}
