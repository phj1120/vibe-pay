package com.vibe.pay.domain.rewardpoints.controller;

import com.vibe.pay.domain.rewardpoints.dto.RewardPointsRequest;
import com.vibe.pay.domain.rewardpoints.dto.RewardPointsResponse;
import com.vibe.pay.domain.rewardpoints.entity.RewardPoints;
import com.vibe.pay.domain.rewardpoints.service.RewardPointsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 리워드 포인트 관리 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/rewardpoints")
@RequiredArgsConstructor
public class RewardPointsController {

    private final RewardPointsService rewardPointsService;

    @PostMapping
    public ResponseEntity<RewardPointsResponse> createRewardPoints(@RequestBody RewardPoints rewardPoints) {
        log.info("Creating reward points for member: {}", rewardPoints.getMemberId());
        RewardPoints created = rewardPointsService.createRewardPoints(rewardPoints);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @GetMapping("/{rewardPointsId}")
    public ResponseEntity<RewardPointsResponse> getRewardPointsById(@PathVariable Long rewardPointsId) {
        log.info("Getting reward points by ID: {}", rewardPointsId);
        return rewardPointsService.getRewardPointsById(rewardPointsId)
                .map(rp -> ResponseEntity.ok(toResponse(rp)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<RewardPointsResponse> getRewardPointsByMemberId(@PathVariable Long memberId) {
        log.info("Getting reward points by member ID: {}", memberId);
        RewardPoints rewardPoints = rewardPointsService.getRewardPointsByMemberId(memberId);
        return rewardPoints != null ? ResponseEntity.ok(toResponse(rewardPoints)) : ResponseEntity.notFound().build();
    }

    @PutMapping("/add")
    public ResponseEntity<RewardPointsResponse> addPoints(@RequestBody RewardPointsRequest request) {
        log.info("Adding {} points for member: {}", request.getPoints(), request.getMemberId());
        try {
            RewardPoints rewardPoints = rewardPointsService.addPoints(request.getMemberId(), request.getPoints());
            return ResponseEntity.ok(toResponse(rewardPoints));
        } catch (RuntimeException e) {
            log.error("Failed to add points: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/use")
    public ResponseEntity<RewardPointsResponse> usePoints(@RequestBody RewardPointsRequest request) {
        log.info("Using {} points for member: {}", request.getPoints(), request.getMemberId());
        try {
            RewardPoints rewardPoints = rewardPointsService.usePoints(request.getMemberId(), request.getPoints());
            return ResponseEntity.ok(toResponse(rewardPoints));
        } catch (IllegalStateException e) {
            log.error("Failed to use points: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    private RewardPointsResponse toResponse(RewardPoints rewardPoints) {
        RewardPointsResponse response = new RewardPointsResponse();
        response.setRewardPointsId(rewardPoints.getRewardPointsId());
        response.setMemberId(rewardPoints.getMemberId());
        response.setPoints(rewardPoints.getPoints());
        response.setLastUpdated(rewardPoints.getLastUpdated());
        return response;
    }
}