package com.vibe.pay.domain.pointhistory.controller;

import com.vibe.pay.domain.pointhistory.dto.PointHistoryResponse;
import com.vibe.pay.domain.pointhistory.entity.PointHistory;
import com.vibe.pay.domain.pointhistory.service.PointHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 포인트 이력 관리 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/point-history")
@RequiredArgsConstructor
public class PointHistoryController {

    private final PointHistoryService pointHistoryService;

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<PointHistoryResponse>> getPointHistoryByMemberId(@PathVariable Long memberId) {
        log.info("Getting point history by member ID: {}", memberId);
        List<PointHistory> histories = pointHistoryService.getPointHistoryByMemberId(memberId);
        return ResponseEntity.ok(histories.stream().map(this::toResponse).collect(Collectors.toList()));
    }

    private PointHistoryResponse toResponse(PointHistory pointHistory) {
        PointHistoryResponse response = new PointHistoryResponse();
        response.setPointHistoryId(pointHistory.getPointHistoryId());
        response.setMemberId(pointHistory.getMemberId());
        response.setPointAmount(pointHistory.getPointAmount());
        response.setBalanceAfter(pointHistory.getBalanceAfter());
        response.setTransactionType(pointHistory.getTransactionType());
        response.setReferenceType(pointHistory.getReferenceType());
        response.setReferenceId(pointHistory.getReferenceId());
        response.setDescription(pointHistory.getDescription());
        response.setCreatedAt(pointHistory.getCreatedAt());
        return response;
    }
}