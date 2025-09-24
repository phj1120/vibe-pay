package com.vibe.pay.backend.rewardpoints;

import com.vibe.pay.backend.pointhistory.PointHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RewardPointsService {
    private final RewardPointsMapper rewardPointsMapper;
    private final PointHistoryService pointHistoryService;

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
    public RewardPoints addPoints(Long memberId, Long pointsToAdd) {
        RewardPoints rewardPoints = rewardPointsMapper.findByMemberId(memberId);
        Long previousBalance = 0L;

        if (rewardPoints == null) {
            rewardPoints = new RewardPoints(memberId, pointsToAdd);
            rewardPointsMapper.insert(rewardPoints);
        } else {
            previousBalance = rewardPoints.getPoints();
            rewardPoints.setPoints(rewardPoints.getPoints() + pointsToAdd);
            rewardPoints.setLastUpdated(LocalDateTime.now());
            rewardPointsMapper.update(rewardPoints);
        }

        // 포인트 충전 내역 기록
        try {
            pointHistoryService.recordPointEarn(
                memberId,
                pointsToAdd,
                "MANUAL_CHARGE", // reference_type
                rewardPoints.getRewardPointsId().toString(), // reward_points_id를 reference_id로 사용
                "마일리지 수동 충전"
            );
            log.info("Point earning recorded: memberId={}, pointsAdded={}, newBalance={}",
                     memberId, pointsToAdd, rewardPoints.getPoints());
        } catch (Exception e) {
            log.error("Failed to record point earning history: memberId={}, pointsAdded={}",
                      memberId, pointsToAdd, e);
            // 포인트 내역 기록 실패해도 충전은 계속 진행
        }

        return rewardPoints;
    }

    @Transactional
    public RewardPoints usePoints(Long memberId, Long pointsToUse) {
        RewardPoints rewardPoints = rewardPointsMapper.findByMemberId(memberId);
        if (rewardPoints == null || rewardPoints.getPoints() < pointsToUse) {
            throw new IllegalStateException("Insufficient reward points for member " + memberId);
        }
        rewardPoints.setPoints(rewardPoints.getPoints() - pointsToUse);
        rewardPoints.setLastUpdated(LocalDateTime.now());
        rewardPointsMapper.update(rewardPoints);
        return rewardPoints;
    }
}