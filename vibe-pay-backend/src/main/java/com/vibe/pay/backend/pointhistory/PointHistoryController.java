package com.vibe.pay.backend.pointhistory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/point-history")
public class PointHistoryController {

    private static final Logger log = LoggerFactory.getLogger(PointHistoryController.class);

    @Autowired
    private PointHistoryService pointHistoryService;

    /**
     * 회원별 포인트 내역 조회
     */
    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<PointHistory>> getPointHistoryByMember(@PathVariable Long memberId) {
        try {
            log.info("Fetching point history for member: {}", memberId);
            List<PointHistory> pointHistory = pointHistoryService.getPointHistoryByMember(memberId);
            log.info("Retrieved {} point history records for member: {}", pointHistory.size(), memberId);
            return ResponseEntity.ok(pointHistory);
        } catch (Exception e) {
            log.error("Error fetching point history for member: {}", memberId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 회원별 포인트 사용 통계 조회
     */
    @GetMapping("/member/{memberId}/statistics")
    public ResponseEntity<PointStatistics> getPointStatistics(@PathVariable Long memberId) {
        try {
            log.info("Fetching point statistics for member: {}", memberId);
            PointStatistics statistics = pointHistoryService.getPointStatistics(memberId);
            log.info("Retrieved point statistics for member: {} - current balance: {}, total earned: {}, total used: {}",
                    memberId, statistics.getCurrentBalance(), statistics.getTotalEarned(), statistics.getTotalUsed());
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Error fetching point statistics for member: {}", memberId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 회원별 특정 거래 타입 포인트 내역 조회
     */
    @GetMapping("/member/{memberId}/type/{transactionType}")
    public ResponseEntity<List<PointHistory>> getPointHistoryByMemberAndType(
            @PathVariable Long memberId,
            @PathVariable String transactionType) {
        try {
            log.info("Fetching point history for member: {} with transaction type: {}", memberId, transactionType);
            List<PointHistory> pointHistory = pointHistoryService.getPointHistoryByMemberAndType(memberId, transactionType);
            log.info("Retrieved {} point history records for member: {} with type: {}",
                    pointHistory.size(), memberId, transactionType);
            return ResponseEntity.ok(pointHistory);
        } catch (Exception e) {
            log.error("Error fetching point history for member: {} with type: {}", memberId, transactionType, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 특정 거래와 관련된 포인트 내역 조회
     */
    @GetMapping("/reference/{referenceType}/{referenceId}")
    public ResponseEntity<List<PointHistory>> getPointHistoryByReference(
            @PathVariable String referenceType,
            @PathVariable String referenceId) {
        try {
            log.info("Fetching point history for reference: {} - {}", referenceType, referenceId);
            List<PointHistory> pointHistory = pointHistoryService.getPointHistoryByReference(referenceType, referenceId);
            log.info("Retrieved {} point history records for reference: {} - {}",
                    pointHistory.size(), referenceType, referenceId);
            return ResponseEntity.ok(pointHistory);
        } catch (Exception e) {
            log.error("Error fetching point history for reference: {} - {}", referenceType, referenceId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 전체 포인트 내역 조회 (관리자용)
     */
    @GetMapping("/all")
    public ResponseEntity<List<PointHistory>> getAllPointHistory() {
        try {
            log.info("Fetching all point history records");
            List<PointHistory> pointHistory = pointHistoryService.getAllPointHistory();
            log.info("Retrieved {} point history records", pointHistory.size());
            return ResponseEntity.ok(pointHistory);
        } catch (Exception e) {
            log.error("Error fetching all point history", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 특정 포인트 히스토리 조회
     */
    @GetMapping("/{pointHistoryId}")
    public ResponseEntity<PointHistory> getPointHistoryById(@PathVariable Long pointHistoryId) {
        try {
            log.info("Fetching point history with ID: {}", pointHistoryId);
            PointHistory pointHistory = pointHistoryService.getPointHistoryById(pointHistoryId);
            if (pointHistory != null) {
                log.info("Retrieved point history with ID: {}", pointHistoryId);
                return ResponseEntity.ok(pointHistory);
            } else {
                log.warn("Point history not found with ID: {}", pointHistoryId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error fetching point history with ID: {}", pointHistoryId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}