package com.vibe.pay.backend.rewardpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rewardpoints")
public class RewardPointsController {

    @Autowired
    private RewardPointsService rewardPointsService;

    @PostMapping
    public RewardPoints createRewardPoints(@RequestBody RewardPoints rewardPoints) {
        return rewardPointsService.createRewardPoints(rewardPoints);
    }

    @GetMapping("/{rewardPointsId}")
    public ResponseEntity<RewardPoints> getRewardPointsById(@PathVariable Long rewardPointsId) {
        return rewardPointsService.getRewardPointsById(rewardPointsId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<RewardPoints> getRewardPointsByMemberId(@PathVariable Long memberId) {
        RewardPoints rewardPoints = rewardPointsService.getRewardPointsByMemberId(memberId);
        if (rewardPoints != null) {
            return ResponseEntity.ok(rewardPoints);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/add")
    public ResponseEntity<RewardPoints> addPoints(@RequestBody RewardPointsRequest request) {
        try {
            RewardPoints updatedRewardPoints = rewardPointsService.addPoints(request.getMemberId(), request.getPoints());
            return ResponseEntity.ok(updatedRewardPoints);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null); // Or a more specific error response
        }
    }

    @PutMapping("/use")
    public ResponseEntity<RewardPoints> usePoints(@RequestBody RewardPointsRequest request) {
        try {
            RewardPoints updatedRewardPoints = rewardPointsService.usePoints(request.getMemberId(), request.getPoints());
            return ResponseEntity.ok(updatedRewardPoints);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null); // Or a more specific error response
        }
    }
}