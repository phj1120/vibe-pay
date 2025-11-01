package com.vibe.pay.backend.pointhistory;

import com.vibe.pay.backend.enums.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PointHistoryService {
    private final PointHistoryMapper pointHistoryMapper;

    /**
     * 포인트 적립 기록
     */
    @Transactional
    public void recordPointEarn(Long memberId, Double pointAmount, String referenceType,
                                String referenceId, String description, Double balanceAfter) {
        log.debug("포인트 적립 기록: memberId={}, amount={}", memberId, pointAmount);

        PointHistory history = PointHistory.builder()
                .memberId(memberId)
                .pointAmount(pointAmount)
                .balanceAfter(balanceAfter)
                .transactionType(TransactionType.EARN.name())
                .referenceType(referenceType)
                .referenceId(referenceId)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();

        pointHistoryMapper.insert(history);
        log.info("포인트 적립 기록 완료: memberId={}, amount={}", memberId, pointAmount);
    }

    /**
     * 포인트 사용 기록
     */
    @Transactional
    public void recordPointUse(Long memberId, Double pointAmount, String referenceType,
                               String referenceId, String description, Double balanceAfter) {
        log.debug("포인트 사용 기록: memberId={}, amount={}", memberId, pointAmount);

        PointHistory history = PointHistory.builder()
                .memberId(memberId)
                .pointAmount(-pointAmount) // 음수로 저장
                .balanceAfter(balanceAfter)
                .transactionType(TransactionType.USE.name())
                .referenceType(referenceType)
                .referenceId(referenceId)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();

        pointHistoryMapper.insert(history);
        log.info("포인트 사용 기록 완료: memberId={}, amount={}", memberId, pointAmount);
    }

    /**
     * 포인트 환불 기록
     */
    @Transactional
    public void recordPointRefund(Long memberId, Double pointAmount, String referenceType,
                                  String referenceId, String description, Double balanceAfter) {
        log.debug("포인트 환불 기록: memberId={}, amount={}", memberId, pointAmount);

        PointHistory history = PointHistory.builder()
                .memberId(memberId)
                .pointAmount(pointAmount)
                .balanceAfter(balanceAfter)
                .transactionType(TransactionType.REFUND.name())
                .referenceType(referenceType)
                .referenceId(referenceId)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();

        pointHistoryMapper.insert(history);
        log.info("포인트 환불 기록 완료: memberId={}, amount={}", memberId, pointAmount);
    }

    /**
     * 회원의 포인트 이력 조회
     */
    public List<PointHistory> getHistoryByMemberId(Long memberId) {
        log.debug("포인트 이력 조회: memberId={}", memberId);
        return pointHistoryMapper.selectByMemberId(memberId);
    }

    /**
     * 회원의 포인트 통계 조회
     */
    public PointStatistics getStatisticsByMemberId(Long memberId) {
        log.debug("포인트 통계 조회: memberId={}", memberId);

        List<PointHistory> histories = pointHistoryMapper.selectByMemberId(memberId);

        double totalEarned = 0.0;
        double totalUsed = 0.0;
        double totalRefunded = 0.0;
        double currentBalance = 0.0;

        for (PointHistory history : histories) {
            String type = history.getTransactionType();
            Double amount = Math.abs(history.getPointAmount());

            if (TransactionType.EARN.name().equals(type)) {
                totalEarned += amount;
            } else if (TransactionType.USE.name().equals(type)) {
                totalUsed += amount;
            } else if (TransactionType.REFUND.name().equals(type)) {
                totalRefunded += amount;
            }
        }

        // 가장 최근 기록에서 현재 잔액 가져오기
        if (!histories.isEmpty()) {
            currentBalance = histories.get(0).getBalanceAfter();
        }

        return PointStatistics.builder()
                .totalEarned(totalEarned)
                .totalUsed(totalUsed)
                .totalRefunded(totalRefunded)
                .currentBalance(currentBalance)
                .build();
    }
}
