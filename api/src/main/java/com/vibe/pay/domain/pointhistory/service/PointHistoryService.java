package com.vibe.pay.domain.pointhistory.service;

import com.vibe.pay.domain.pointhistory.entity.PointHistory;
import com.vibe.pay.domain.pointhistory.repository.PointHistoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 포인트 이력 서비스
 * 포인트 적립 및 사용 이력을 기록하고 조회하는 비즈니스 로직을 처리하는 계층
 *
 * Technical Specification 참조:
 * - docs/v5/TechnicalSpecification/point-history-spec.md
 *
 * @see PointHistory
 * @see PointHistoryMapper
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PointHistoryService {

    private final PointHistoryMapper pointHistoryMapper;

    /**
     * 포인트 적립 이력 기록
     *
     * @param memberId 회원 ID
     * @param pointAmount 적립 포인트
     * @param balanceAfter 적립 후 잔액
     * @param referenceType 참조 타입 (INITIAL, ORDER, REFUND 등)
     * @param referenceId 참조 ID (주문 번호, 클레임 번호 등)
     * @param description 설명
     * @throws RuntimeException 이력 기록 실패 시
     */
    @Transactional
    public void recordPointEarn(Long memberId, Long pointAmount, Long balanceAfter,
                                  String referenceType, String referenceId, String description) {
        log.debug("Recording point earn: memberId={}, amount={}, balanceAfter={}",
                memberId, pointAmount, balanceAfter);

        PointHistory history = new PointHistory();
        history.setMemberId(memberId);
        history.setPointAmount(pointAmount);
        history.setTransactionType("EARN");
        history.setBalanceAfter(balanceAfter);
        history.setReferenceType(referenceType);
        history.setReferenceId(referenceId);
        history.setDescription(description);
        history.setCreatedAt(LocalDateTime.now());

        pointHistoryMapper.insert(history);
        log.debug("Point earn recorded: pointHistoryId={}", history.getPointHistoryId());
    }

    /**
     * 포인트 사용 이력 기록
     *
     * @param memberId 회원 ID
     * @param pointAmount 사용 포인트 (양수)
     * @param balanceAfter 사용 후 잔액
     * @param referenceType 참조 타입 (ORDER, REFUND 등)
     * @param referenceId 참조 ID (주문 번호, 클레임 번호 등)
     * @param description 설명
     * @throws RuntimeException 이력 기록 실패 시
     */
    @Transactional
    public void recordPointUse(Long memberId, Long pointAmount, Long balanceAfter,
                                 String referenceType, String referenceId, String description) {
        log.debug("Recording point use: memberId={}, amount={}, balanceAfter={}",
                memberId, pointAmount, balanceAfter);

        PointHistory history = new PointHistory();
        history.setMemberId(memberId);
        history.setPointAmount(pointAmount);
        history.setTransactionType("USE");
        history.setBalanceAfter(balanceAfter);
        history.setReferenceType(referenceType);
        history.setReferenceId(referenceId);
        history.setDescription(description);
        history.setCreatedAt(LocalDateTime.now());

        pointHistoryMapper.insert(history);
        log.debug("Point use recorded: pointHistoryId={}", history.getPointHistoryId());
    }

    /**
     * 회원별 포인트 이력 조회
     *
     * @param memberId 회원 ID
     * @return 포인트 이력 엔티티 목록
     */
    public List<PointHistory> getPointHistoryByMemberId(Long memberId) {
        log.debug("Fetching point history by memberId: {}", memberId);
        return pointHistoryMapper.findByMemberId(memberId);
    }
}
