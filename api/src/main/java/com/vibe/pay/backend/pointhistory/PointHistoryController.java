package com.vibe.pay.backend.pointhistory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
@RestController
@RequestMapping("/api/pointhistory")
@Slf4j
@RequiredArgsConstructor
public class PointHistoryController {
    private final PointHistoryService pointHistoryService;

    /**
     * 회원의 포인트 이력 조회
     */
    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<PointHistory>> getPointHistory(@PathVariable Long memberId) {
        log.debug("포인트 이력 조회 요청: memberId={}", memberId);

        List<PointHistory> history = pointHistoryService.getHistoryByMemberId(memberId);
        return ResponseEntity.ok(history);
    }

    /**
     * 회원의 포인트 통계 조회
     */
    @GetMapping("/member/{memberId}/statistics")
    public ResponseEntity<PointStatistics> getPointStatistics(@PathVariable Long memberId) {
        log.debug("포인트 통계 조회 요청: memberId={}", memberId);

        PointStatistics statistics = pointHistoryService.getStatisticsByMemberId(memberId);
        return ResponseEntity.ok(statistics);
    }
}
