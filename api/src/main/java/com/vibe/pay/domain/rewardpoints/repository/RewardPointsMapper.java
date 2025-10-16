package com.vibe.pay.domain.rewardpoints.repository;

import com.vibe.pay.domain.rewardpoints.entity.RewardPoints;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Optional;

@Mapper
public interface RewardPointsMapper {
    void insert(RewardPoints rewardPoints);
    Optional<RewardPoints> findByRewardPointsId(Long rewardPointsId);
    Optional<RewardPoints> findByMemberId(Long memberId);
    List<RewardPoints> findAll();
    void update(RewardPoints rewardPoints);
    void delete(Long rewardPointsId);
}
