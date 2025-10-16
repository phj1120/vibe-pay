package com.vibe.pay.domain.rewardpoints.service;

import com.vibe.pay.domain.pointhistory.service.PointHistoryService;
import com.vibe.pay.domain.rewardpoints.entity.RewardPoints;
import com.vibe.pay.domain.rewardpoints.repository.RewardPointsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 리워드 포인트 서비스
 * 회원의 리워드 포인트 적립, 사용, 조회 등의 비즈니스 로직을 처리하는 계층
 *
 * Technical Specification 참조:
 * - docs/v5/TechnicalSpecification/reward-points-management-spec.md
 *
 * @see RewardPoints
 * @see RewardPointsMapper
 * @see PointHistoryService
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RewardPointsService {

    private final RewardPointsMapper rewardPointsMapper;
    private final PointHistoryService pointHistoryService;

    /**
     * 리워드 포인트 생성
     *
     * @param rewardPoints 생성할 리워드 포인트 엔티티
     * @return 생성된 리워드 포인트 엔티티
     * @throws RuntimeException 리워드 포인트 생성 실패 시
     */
    @Transactional
    public RewardPoints createRewardPoints(RewardPoints rewardPoints) {
        log.info("Creating reward points: memberId={}", rewardPoints.getMemberId());
        rewardPointsMapper.insert(rewardPoints);
        log.info("Reward points created: rewardPointsId={}", rewardPoints.getRewardPointsId());
        return rewardPoints;
    }

    /**
     * 리워드 포인트 ID로 조회
     *
     * @param rewardPointsId 리워드 포인트 ID
     * @return 조회된 리워드 포인트 엔티티 (Optional)
     */
    public Optional<RewardPoints> getRewardPointsById(Long rewardPointsId) {
        log.debug("Fetching reward points by ID: rewardPointsId={}", rewardPointsId);
        return rewardPointsMapper.findById(rewardPointsId);
    }

    /**
     * 회원 ID로 리워드 포인트 조회
     *
     * @param memberId 회원 ID
     * @return 조회된 리워드 포인트 엔티티
     * @throws RuntimeException 리워드 포인트를 찾을 수 없을 때
     */
    public RewardPoints getRewardPointsByMemberId(Long memberId) {
        log.debug("Fetching reward points by memberId: {}", memberId);
        return rewardPointsMapper.findByMemberId(memberId)
                .orElseThrow(() -> new RuntimeException("Reward points not found for member: " + memberId));
    }

    /**
     * 포인트 적립
     *
     * 포인트를 적립하고 이력을 기록합니다.
     * 회원의 포인트 정보가 없는 경우 새로 생성합니다.
     *
     * @param memberId 회원 ID
     * @param pointsToAdd 적립할 포인트
     * @return 업데이트된 리워드 포인트 엔티티
     * @throws RuntimeException 포인트 적립 실패 시
     */
    @Transactional
    public RewardPoints addPoints(Long memberId, Long pointsToAdd) {
        log.info("Adding points: memberId={}, pointsToAdd={}", memberId, pointsToAdd);

        Optional<RewardPoints> optionalRewardPoints = rewardPointsMapper.findByMemberId(memberId);

        RewardPoints rewardPoints;
        if (optionalRewardPoints.isEmpty()) {
            // 포인트 정보가 없으면 새로 생성
            log.debug("No existing reward points found, creating new: memberId={}", memberId);
            rewardPoints = new RewardPoints();
            rewardPoints.setMemberId(memberId);
            rewardPoints.setCurrentPoints(pointsToAdd);
            rewardPoints.setTotalEarnedPoints(pointsToAdd);
            rewardPoints.setTotalUsedPoints(0L);
            rewardPointsMapper.insert(rewardPoints);
        } else {
            // 기존 포인트에 적립
            rewardPoints = optionalRewardPoints.get();
            rewardPoints.setCurrentPoints(rewardPoints.getCurrentPoints() + pointsToAdd);
            rewardPoints.setTotalEarnedPoints(rewardPoints.getTotalEarnedPoints() + pointsToAdd);
            rewardPointsMapper.update(rewardPoints);
        }

        // 포인트 이력 기록
        pointHistoryService.recordPointEarn(
                memberId,
                pointsToAdd,
                rewardPoints.getCurrentPoints(),
                "INITIAL",
                null,
                "포인트 적립"
        );

        log.info("Points added successfully: memberId={}, newBalance={}", memberId, rewardPoints.getCurrentPoints());
        return rewardPoints;
    }

    /**
     * 포인트 사용
     *
     * 포인트를 사용하고 이력을 기록합니다.
     * 잔액이 부족한 경우 예외를 발생시킵니다.
     *
     * @param memberId 회원 ID
     * @param pointsToUse 사용할 포인트
     * @return 업데이트된 리워드 포인트 엔티티
     * @throws RuntimeException 포인트 잔액 부족 또는 사용 실패 시
     */
    @Transactional
    public RewardPoints usePoints(Long memberId, Long pointsToUse) {
        log.info("Using points: memberId={}, pointsToUse={}", memberId, pointsToUse);

        RewardPoints rewardPoints = rewardPointsMapper.findByMemberId(memberId)
                .orElseThrow(() -> new RuntimeException("Reward points not found for member: " + memberId));

        // 잔액 확인
        if (rewardPoints.getCurrentPoints() < pointsToUse) {
            log.warn("Insufficient points: memberId={}, currentPoints={}, pointsToUse={}",
                    memberId, rewardPoints.getCurrentPoints(), pointsToUse);
            throw new RuntimeException("Insufficient points: current=" + rewardPoints.getCurrentPoints()
                    + ", required=" + pointsToUse);
        }

        // 포인트 차감
        rewardPoints.setCurrentPoints(rewardPoints.getCurrentPoints() - pointsToUse);
        rewardPoints.setTotalUsedPoints(rewardPoints.getTotalUsedPoints() + pointsToUse);
        rewardPointsMapper.update(rewardPoints);

        // 포인트 이력 기록
        pointHistoryService.recordPointUse(
                memberId,
                pointsToUse,
                rewardPoints.getCurrentPoints(),
                "ORDER",
                null,
                "포인트 사용"
        );

        log.info("Points used successfully: memberId={}, newBalance={}", memberId, rewardPoints.getCurrentPoints());
        return rewardPoints;
    }
}
