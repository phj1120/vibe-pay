package com.vibe.pay.backend.pointhistory;

import com.vibe.pay.backend.payment.Payment;
import com.vibe.pay.backend.paymentlog.PaymentInterfaceRequestLog;
import com.vibe.pay.backend.rewardpoints.RewardPointsMapper;
import com.vibe.pay.backend.rewardpoints.RewardPoints;
import com.vibe.pay.backend.util.HashUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class PointHistoryService {

    private final PointHistoryMapper pointHistoryMapper;
    private final RewardPointsMapper rewardPointsMapper;

    /**
     * 포인트 사용 내역 기록 (결제 시)
     */
    @Transactional
    public void recordPointUsage(Long memberId, Long usedPoints, String referenceId, String description) {
        if (usedPoints == null || usedPoints <= 0) {
            log.debug("No points used, skipping point history record for member: {}", memberId);
            return;
        }

        // 현재 포인트 잔액 조회
        RewardPoints currentRewardPoints = rewardPointsMapper.findByMemberId(memberId);
        if (currentRewardPoints == null) {
            log.error("RewardPoints not found for member: {}", memberId);
            throw new IllegalStateException("Member reward points not found");
        }

        // 사용 내역 기록
        PointHistory pointHistory = new PointHistory();
        pointHistory.setMemberId(memberId);
        pointHistory.setPointAmount(-usedPoints);
        pointHistory.setBalanceAfter(currentRewardPoints.getPoints());
        pointHistory.setTransactionType("USE");
        pointHistory.setReferenceType("PAYMENT");
        pointHistory.setReferenceId(referenceId);
        pointHistory.setDescription(description);
        pointHistory.setCreatedAt(LocalDateTime.now());

        pointHistoryMapper.insert(pointHistory);
        log.info("Point usage recorded: memberId={}, usedPoints={}, balance={}, referenceId={}",
                memberId, usedPoints, currentRewardPoints.getPoints(), referenceId);
    }

    /**
     * 포인트 복원 내역 기록 (취소 시)
     */
    @Transactional
    public void recordPointRefund(Long memberId, Long refundPoints, String referenceId, String description) {
        if (refundPoints == null || refundPoints <= 0) {
            log.debug("No points to refund, skipping point history record for member: {}", memberId);
            return;
        }

        // 현재 포인트 잔액 조회
        RewardPoints currentRewardPoints = rewardPointsMapper.findByMemberId(memberId);
        if (currentRewardPoints == null) {
            log.error("RewardPoints not found for member: {}", memberId);
            throw new IllegalStateException("Member reward points not found");
        }

        // 복원 내역 기록
        PointHistory pointHistory = new PointHistory();
        pointHistory.setMemberId(memberId);
        pointHistory.setPointAmount(refundPoints);
        pointHistory.setBalanceAfter(currentRewardPoints.getPoints());
        pointHistory.setTransactionType("REFUND");
        pointHistory.setReferenceType("CANCEL");
        pointHistory.setReferenceId(referenceId);
        pointHistory.setDescription(description);
        pointHistory.setCreatedAt(LocalDateTime.now());

        pointHistoryMapper.insert(pointHistory);
        log.info("Point refund recorded: memberId={}, refundPoints={}, balance={}, referenceId={}",
                memberId, refundPoints, currentRewardPoints.getPoints(), referenceId);
    }

    /**
     * 포인트 적립 내역 기록
     */
    @Transactional
    public void recordPointEarn(Long memberId, Long earnedPoints, String referenceType, String referenceId, String description) {
        if (earnedPoints == null || earnedPoints <= 0) {
            log.debug("No points earned, skipping point history record for member: {}", memberId);
            return;
        }

        // 현재 포인트 잔액 조회
        RewardPoints currentRewardPoints = rewardPointsMapper.findByMemberId(memberId);
        if (currentRewardPoints == null) {
            log.error("RewardPoints not found for member: {}", memberId);
            throw new IllegalStateException("Member reward points not found");
        }

        // 적립 내역 기록 (양수로 기록)
        PointHistory pointHistory = new PointHistory();
        pointHistory.setMemberId(memberId);
        pointHistory.setPointAmount(earnedPoints);
        pointHistory.setBalanceAfter(currentRewardPoints.getPoints());
        pointHistory.setTransactionType("REFUND");
        pointHistory.setReferenceType("CANCEL");
        pointHistory.setReferenceId(referenceId);
        pointHistory.setDescription(description);
        pointHistory.setCreatedAt(LocalDateTime.now());

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