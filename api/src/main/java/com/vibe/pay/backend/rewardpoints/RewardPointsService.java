package com.vibe.pay.backend.rewardpoints;

import com.vibe.pay.backend.exception.InsufficientPointsException;
import com.vibe.pay.backend.exception.MemberNotFoundException;
import com.vibe.pay.backend.pointhistory.PointHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RewardPointsService {
    private final RewardPointsMapper rewardPointsMapper;
    private final PointHistoryService pointHistoryService;

    /**
     * 리워드 포인트 생성
     */
    @Transactional
    public RewardPoints createRewardPoints(RewardPoints rewardPoints) {
        log.debug("포인트 생성 시작: memberId={}", rewardPoints.getMemberId());

        RewardPoints newPoints = RewardPoints.builder()
                .memberId(rewardPoints.getMemberId())
                .points(rewardPoints.getPoints() != null ? rewardPoints.getPoints() : 0.0)
                .lastUpdated(LocalDateTime.now())
                .build();

        rewardPointsMapper.insert(newPoints);
        log.info("포인트 생성 완료: memberId={}, points={}", newPoints.getMemberId(), newPoints.getPoints());

        return rewardPointsMapper.selectByMemberId(newPoints.getMemberId());
    }

    /**
     * ID로 리워드 포인트 조회
     */
    public Optional<RewardPoints> getRewardPointsById(Long rewardPointsId) {
        log.debug("포인트 조회: rewardPointsId={}", rewardPointsId);
        RewardPoints points = rewardPointsMapper.selectById(rewardPointsId);
        return Optional.ofNullable(points);
    }

    /**
     * 회원 ID로 리워드 포인트 조회
     */
    public RewardPoints getRewardPointsByMemberId(Long memberId) {
        log.debug("회원 포인트 조회: memberId={}", memberId);
        return rewardPointsMapper.selectByMemberId(memberId);
    }

    /**
     * 포인트 적립
     */
    @Transactional
    public RewardPoints addPoints(Long memberId, Double pointsToAdd) {
        log.debug("포인트 적립 시작: memberId={}, amount={}", memberId, pointsToAdd);

        if (pointsToAdd < 0) {
            throw new IllegalArgumentException("적립 포인트는 음수일 수 없습니다: " + pointsToAdd);
        }

        // 기존 포인트 조회 또는 생성
        RewardPoints existingPoints = rewardPointsMapper.selectByMemberId(memberId);
        if (existingPoints == null) {
            existingPoints = createRewardPoints(RewardPoints.builder()
                    .memberId(memberId)
                    .points(0.0)
                    .build());
        }

        // 포인트 추가
        Double newBalance = existingPoints.getPoints() + pointsToAdd;
        RewardPoints updatedPoints = RewardPoints.builder()
                .rewardPointsId(existingPoints.getRewardPointsId())
                .memberId(memberId)
                .points(newBalance)
                .lastUpdated(LocalDateTime.now())
                .build();

        rewardPointsMapper.update(updatedPoints);

        // 이력 기록
        pointHistoryService.recordPointEarn(memberId, pointsToAdd, "MANUAL_CHARGE",
                String.valueOf(memberId), "포인트 적립", newBalance);

        log.info("포인트 적립 완료: memberId={}, amount={}, newBalance={}", memberId, pointsToAdd, newBalance);

        return rewardPointsMapper.selectByMemberId(memberId);
    }

    /**
     * 포인트 사용
     */
    @Transactional
    public RewardPoints usePoints(Long memberId, Double pointsToUse) {
        log.debug("포인트 사용 시작: memberId={}, amount={}", memberId, pointsToUse);

        // 기존 포인트 조회
        RewardPoints existingPoints = rewardPointsMapper.selectByMemberId(memberId);
        if (existingPoints == null) {
            throw new MemberNotFoundException("회원의 포인트 정보를 찾을 수 없습니다: memberId=" + memberId);
        }

        // 잔액 확인
        if (existingPoints.getPoints() < pointsToUse) {
            throw new InsufficientPointsException(
                    String.format("포인트 잔액이 부족합니다. 현재: %.2f, 사용 요청: %.2f",
                            existingPoints.getPoints(), pointsToUse));
        }

        // 포인트 차감
        Double newBalance = existingPoints.getPoints() - pointsToUse;
        RewardPoints updatedPoints = RewardPoints.builder()
                .rewardPointsId(existingPoints.getRewardPointsId())
                .memberId(memberId)
                .points(newBalance)
                .lastUpdated(LocalDateTime.now())
                .build();

        rewardPointsMapper.update(updatedPoints);

        // 이력 기록
        pointHistoryService.recordPointUse(memberId, pointsToUse, "MANUAL",
                String.valueOf(memberId), "포인트 사용", newBalance);

        log.info("포인트 사용 완료: memberId={}, amount={}, newBalance={}", memberId, pointsToUse, newBalance);

        return rewardPointsMapper.selectByMemberId(memberId);
    }

    /**
     * 포인트 환불
     */
    @Transactional
    public RewardPoints refundPoints(Long memberId, Double pointsToRefund) {
        log.debug("포인트 환불 시작: memberId={}, amount={}", memberId, pointsToRefund);

        // 기존 포인트 조회
        RewardPoints existingPoints = rewardPointsMapper.selectByMemberId(memberId);
        if (existingPoints == null) {
            throw new MemberNotFoundException("회원의 포인트 정보를 찾을 수 없습니다: memberId=" + memberId);
        }

        // 포인트 추가
        Double newBalance = existingPoints.getPoints() + pointsToRefund;
        RewardPoints updatedPoints = RewardPoints.builder()
                .rewardPointsId(existingPoints.getRewardPointsId())
                .memberId(memberId)
                .points(newBalance)
                .lastUpdated(LocalDateTime.now())
                .build();

        rewardPointsMapper.update(updatedPoints);

        // 이력 기록
        pointHistoryService.recordPointRefund(memberId, pointsToRefund, "CANCEL",
                String.valueOf(memberId), "포인트 환불", newBalance);

        log.info("포인트 환불 완료: memberId={}, amount={}, newBalance={}", memberId, pointsToRefund, newBalance);

        return rewardPointsMapper.selectByMemberId(memberId);
    }
}
