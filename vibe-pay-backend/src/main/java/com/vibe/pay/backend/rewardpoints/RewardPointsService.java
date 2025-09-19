package com.vibe.pay.backend.rewardpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class RewardPointsService {

    @Autowired
    private RewardPointsMapper rewardPointsMapper;

    public RewardPoints createRewardPoints(RewardPoints rewardPoints) {
        rewardPoints.setLastUpdated(LocalDateTime.now());
        rewardPointsMapper.insert(rewardPoints);
        return rewardPoints;
    }

    public Optional<RewardPoints> getRewardPointsById(Long rewardPointsId) {
        return Optional.ofNullable(rewardPointsMapper.findByRewardPointsId(rewardPointsId));
    }

    public RewardPoints getRewardPointsByMemberId(Long memberId) {
        return rewardPointsMapper.findByMemberId(memberId);
    }

    @Transactional
    public RewardPoints addPoints(Long memberId, Double pointsToAdd) {
        RewardPoints rewardPoints = rewardPointsMapper.findByMemberId(memberId);
        if (rewardPoints == null) {
            rewardPoints = new RewardPoints(memberId, pointsToAdd);
            rewardPointsMapper.insert(rewardPoints);
        } else {
            rewardPoints.setPoints(rewardPoints.getPoints() + pointsToAdd);
            rewardPoints.setLastUpdated(LocalDateTime.now());
            rewardPointsMapper.update(rewardPoints);
        }
        return rewardPoints;
    }

    @Transactional
    public RewardPoints usePoints(Long memberId, Double pointsToUse) {
        RewardPoints rewardPoints = rewardPointsMapper.findByMemberId(memberId);
        if (rewardPoints == null || rewardPoints.getPoints() < pointsToUse) {
            throw new RuntimeException("Insufficient reward points for member " + memberId);
        }
        rewardPoints.setPoints(rewardPoints.getPoints() - pointsToUse);
        rewardPoints.setLastUpdated(LocalDateTime.now());
        rewardPointsMapper.update(rewardPoints);
        return rewardPoints;
    }
}