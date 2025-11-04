package com.api.app.controller;

import com.api.app.point.request.PointTransactionRequestDto;
import com.api.app.point.response.PointHistoryResponseDto;
import com.api.app.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    private String getMemberNo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Assuming the principal is the email, and we need to map it to memberNo
        // For now, let's just return the email as a placeholder for memberNo
        // In a real application, you would fetch memberNo from a service using the email
        return authentication.getName(); // This is the email
    }

    @PostMapping("/earn")
    public ResponseEntity<Void> earnPoints(@RequestBody PointTransactionRequestDto request) {
        String memberNo = getMemberNo(); // Replace with actual memberNo from authenticated user
        pointService.earnPoints(memberNo, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/use")
    public ResponseEntity<Void> usePoints(@RequestBody PointTransactionRequestDto request) {
        String memberNo = getMemberNo(); // Replace with actual memberNo from authenticated user
        pointService.usePoints(memberNo, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/history")
    public ResponseEntity<List<PointHistoryResponseDto>> getPointHistory() {
        String memberNo = getMemberNo(); // Replace with actual memberNo from authenticated user
        List<PointHistoryResponseDto> history = pointService.getPointHistory(memberNo);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/total")
    public ResponseEntity<Long> getTotalAvailablePoints() {
        String memberNo = getMemberNo(); // Replace with actual memberNo from authenticated user
        Long totalPoints = pointService.getTotalAvailablePoints(memberNo);
        return ResponseEntity.ok(totalPoints);
    }
}
