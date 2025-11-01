package com.vibe.pay.backend.rewardpoints;

import com.vibe.pay.backend.exception.InsufficientPointsException;
import com.vibe.pay.backend.exception.MemberNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
@RestController
@RequestMapping("/api/rewardpoints")
@Slf4j
@RequiredArgsConstructor
public class RewardPointsController {
    private final RewardPointsService rewardPointsService;

    /**
     * 회원의 리워드 포인트 조회
     */
    @GetMapping("/member/{memberId}")
    public ResponseEntity<RewardPoints> getRewardPointsByMemberId(@PathVariable Long memberId) {
        log.debug("회원 포인트 조회 요청: memberId={}", memberId);

        RewardPoints points = rewardPointsService.getRewardPointsByMemberId(memberId);
        if (points == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(points);
    }

    /**
     * 포인트 적립
     */
    @PostMapping("/member/{memberId}/earn")
    public ResponseEntity<RewardPoints> earnPoints(
            @PathVariable Long memberId,
            @RequestBody PointActionRequest request) {
        log.debug("포인트 적립 요청: memberId={}, amount={}", memberId, request.getPointAmount());

        try {
            RewardPoints points = rewardPointsService.addPoints(memberId, request.getPointAmount());
            return ResponseEntity.ok(points);
        } catch (IllegalArgumentException e) {
            log.error("포인트 적립 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 포인트 사용
     */
    @PostMapping("/member/{memberId}/use")
    public ResponseEntity<RewardPoints> usePoints(
            @PathVariable Long memberId,
            @RequestBody PointActionRequest request) {
        log.debug("포인트 사용 요청: memberId={}, amount={}", memberId, request.getPointAmount());

        try {
            RewardPoints points = rewardPointsService.usePoints(memberId, request.getPointAmount());
            return ResponseEntity.ok(points);
        } catch (MemberNotFoundException e) {
            log.error("회원을 찾을 수 없음: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (InsufficientPointsException e) {
            log.error("포인트 부족: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
