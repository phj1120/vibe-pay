package com.api.app.controller.point;

import com.api.app.common.response.ApiResponse;
import com.api.app.dto.request.point.PointHistoryRequest;
import com.api.app.dto.request.point.PointTransactionRequest;
import com.api.app.dto.response.point.PointBalanceResponse;
import com.api.app.dto.response.point.PointHistoryListResponse;
import com.api.app.service.point.PointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 포인트 관리 컨트롤러
 *
 * @author system
 * @version 1.0
 * @since 2025-10-29
 */
@Slf4j
@RestController
@RequestMapping("/api/point")
@RequiredArgsConstructor
@Tag(name = "포인트 관리", description = "포인트 충전/사용, 잔액 조회, 내역 조회 API")
public class PointController {

    private final PointService pointService;

    /**
     * 포인트 충전/사용 처리
     *
     * @param userDetails 인증된 사용자 정보
     * @param request 포인트 거래 요청
     * @return 성공 응답
     */
    @Operation(summary = "포인트 충전/사용", description = "포인트를 충전하거나 사용합니다")
    @PostMapping("/transaction")
    public ApiResponse<Void> processPointTransaction(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid PointTransactionRequest request) {
        log.info("포인트 거래 요청: email={}, code={}, amount={}",
                userDetails.getUsername(), request.getPointTransactionCode(), request.getAmount());
        pointService.processPointTransaction(userDetails.getUsername(), request);
        return ApiResponse.success();
    }

    /**
     * 보유 포인트 조회
     *
     * @param userDetails 인증된 사용자 정보
     * @return 보유 포인트
     */
    @Operation(summary = "보유 포인트 조회", description = "현재 사용 가능한 포인트를 조회합니다")
    @GetMapping("/balance")
    public ApiResponse<PointBalanceResponse> getPointBalance(
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("보유 포인트 조회 요청: email={}", userDetails.getUsername());
        PointBalanceResponse response = pointService.getPointBalance(userDetails.getUsername());
        return ApiResponse.success(response);
    }

    /**
     * 포인트 내역 목록 조회
     *
     * @param userDetails 인증된 사용자 정보
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 포인트 내역 목록
     */
    @Operation(summary = "포인트 내역 조회", description = "포인트 충전/사용 내역을 페이징 조회합니다")
    @GetMapping("/history")
    public ApiResponse<PointHistoryListResponse> getPointHistoryList(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") Integer size) {
        log.info("포인트 내역 조회 요청: email={}, page={}, size={}",
                userDetails.getUsername(), page, size);

        PointHistoryRequest request = new PointHistoryRequest();
        request.setPage(page);
        request.setSize(size);

        PointHistoryListResponse response = pointService.getPointHistoryList(
                userDetails.getUsername(), request);
        return ApiResponse.success(response);
    }
}
