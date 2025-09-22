package com.vibe.pay.backend.pointhistory;

import com.vibe.pay.backend.rewardpoints.RewardPointsMapper;
import com.vibe.pay.backend.rewardpoints.RewardPoints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PointHistoryService {

    private static final Logger log = LoggerFactory.getLogger(PointHistoryService.class);

    @Autowired
    private PointHistoryMapper pointHistoryMapper;

    @Autowired
    private RewardPointsMapper rewardPointsMapper;

    /**
     * 포인트 사용 내역 기록 (결제 시)
     */
    @Transactional
    public void recordPointUsage(Long memberId, Double usedPoints, String referenceId, String description) {
        if (usedPoints == null || usedPoints <= 0) {
            log.debug("No points used, skipping point history record for member: {}", memberId);
            return;
        }

        // 현재 포인트 잔액 조회
        RewardPoints currentRewardPoints = rewardPointsMapper.findByMemberId(memberId);
        if (currentRewardPoints == null) {
            log.error("RewardPoints not found for member: {}", memberId);
            throw new RuntimeException("Member reward points not found");
        }

        // 사용 내역 기록 (음수로 기록)
        PointHistory pointHistory = new PointHistory(
                memberId,
                -usedPoints, // 사용이므로 음수
                currentRewardPoints.getPoints(), // 현재 잔액 (이미 차감된 상태)
                "USE",
                "PAYMENT",
                referenceId,
                description
        );

        pointHistoryMapper.insert(pointHistory);
        log.info("Point usage recorded: memberId={}, usedPoints={}, balance={}, referenceId={}",
                memberId, usedPoints, currentRewardPoints.getPoints(), referenceId);
    }

    /**
     * 포인트 복원 내역 기록 (취소 시)
     */
    @Transactional
    public void recordPointRefund(Long memberId, Double refundPoints, String referenceId, String description) {
        if (refundPoints == null || refundPoints <= 0) {
            log.debug("No points to refund, skipping point history record for member: {}", memberId);
            return;
        }

        // 현재 포인트 잔액 조회
        RewardPoints currentRewardPoints = rewardPointsMapper.findByMemberId(memberId);
        if (currentRewardPoints == null) {
            log.error("RewardPoints not found for member: {}", memberId);
            throw new RuntimeException("Member reward points not found");
        }

        // 복원 내역 기록 (양수로 기록)
        PointHistory pointHistory = new PointHistory(
                memberId,
                refundPoints, // 복원이므로 양수
                currentRewardPoints.getPoints(), // 현재 잔액 (이미 복원된 상태)
                "REFUND",
                "CANCEL",
                referenceId,
                description
        );

        pointHistoryMapper.insert(pointHistory);
        log.info("Point refund recorded: memberId={}, refundPoints={}, balance={}, referenceId={}",
                memberId, refundPoints, currentRewardPoints.getPoints(), referenceId);
    }

    /**
     * 포인트 적립 내역 기록
     */
    @Transactional
    public void recordPointEarn(Long memberId, Double earnedPoints, String referenceType, String referenceId, String description) {
        if (earnedPoints == null || earnedPoints <= 0) {
            log.debug("No points earned, skipping point history record for member: {}", memberId);
            return;
        }

        // 현재 포인트 잔액 조회
        RewardPoints currentRewardPoints = rewardPointsMapper.findByMemberId(memberId);
        if (currentRewardPoints == null) {
            log.error("RewardPoints not found for member: {}", memberId);
            throw new RuntimeException("Member reward points not found");
        }

        // 적립 내역 기록 (양수로 기록)
        PointHistory pointHistory = new PointHistory(
                memberId,
                earnedPoints, // 적립이므로 양수
                currentRewardPoints.getPoints(), // 현재 잔액 (이미 적립된 상태)
                "EARN",
                referenceType,
                referenceId,
                description
        );

        pointHistoryMapper.insert(pointHistory);
        log.info("Point earn recorded: memberId={}, earnedPoints={}, balance={}, referenceId={}",
                memberId, earnedPoints, currentRewardPoints.getPoints(), referenceId);
    }

    /**
     * 회원별 포인트 내역 조회
     */
    public List<PointHistory> getPointHistoryByMember(Long memberId) {
        return pointHistoryMapper.findByMemberId(memberId);
    }

    /**
     * 회원별 포인트 내역 페이징 조회
     */
    public List<PointHistory> getPointHistoryByMemberWithPaging(Long memberId, int page, int size) {
        int offset = page * size;
        return pointHistoryMapper.findByMemberIdWithPaging(memberId, offset, size);
    }

    /**
     * 특정 거래와 관련된 포인트 내역 조회
     */
    public List<PointHistory> getPointHistoryByReference(String referenceType, String referenceId) {
        return pointHistoryMapper.findByReferenceTypeAndId(referenceType, referenceId);
    }

    /**
     * 회원별 특정 거래 타입 포인트 내역 조회
     */
    public List<PointHistory> getPointHistoryByMemberAndType(Long memberId, String transactionType) {
        return pointHistoryMapper.findByMemberIdAndTransactionType(memberId, transactionType);
    }

    /**
     * 전체 포인트 내역 조회
     */
    public List<PointHistory> getAllPointHistory() {
        return pointHistoryMapper.findAll();
    }

    /**
     * 특정 포인트 히스토리 조회
     */
    public PointHistory getPointHistoryById(Long pointHistoryId) {
        return pointHistoryMapper.findByPointHistoryId(pointHistoryId);
    }

    /**
     * 회원의 포인트 사용 통계 조회
     */
    public PointStatistics getPointStatistics(Long memberId) {
        List<PointHistory> allHistory = pointHistoryMapper.findByMemberId(memberId);

        double totalEarned = 0.0;
        double totalUsed = 0.0;
        double totalRefunded = 0.0;
        int earnCount = 0;
        int useCount = 0;
        int refundCount = 0;

        for (PointHistory history : allHistory) {
            switch (history.getTransactionType()) {
                case "EARN":
                    totalEarned += history.getPointAmount();
                    earnCount++;
                    break;
                case "USE":
                    totalUsed += Math.abs(history.getPointAmount()); // USE는 음수로 저장되므로 절대값
                    useCount++;
                    break;
                case "REFUND":
                    totalRefunded += history.getPointAmount();
                    refundCount++;
                    break;
            }
        }

        // 현재 잔액 조회
        RewardPoints currentRewardPoints = rewardPointsMapper.findByMemberId(memberId);
        double currentBalance = currentRewardPoints != null ? currentRewardPoints.getPoints() : 0.0;

        return new PointStatistics(
                memberId,
                currentBalance,
                totalEarned,
                totalUsed,
                totalRefunded,
                earnCount,
                useCount,
                refundCount,
                allHistory.size()
        );
    }
}