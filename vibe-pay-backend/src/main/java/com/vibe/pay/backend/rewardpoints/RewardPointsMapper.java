package com.vibe.pay.backend.rewardpoints;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface RewardPointsMapper {
    List<RewardPoints> findAll();
    RewardPoints findByRewardPointsId(Long rewardPointsId);
    RewardPoints findByMemberId(Long memberId);
    void insert(RewardPoints rewardPoints);
    void update(RewardPoints rewardPoints);
    void delete(Long rewardPointsId);
}