package com.api.app.controller.point;

import com.api.app.common.response.ApiResponse;
import com.api.app.common.security.SecurityUtils;
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
    private final SecurityUtils securityUtils;

    /**
     * 포인트 충전/사용 처리
     *
     * @param request 포인트 거래 요청
     * @return 성공 응답
     */
    @Operation(summary = "포인트 충전/사용", description = "포인트를 충전하거나 사용합니다")
    @PostMapping("/transaction")
    public ApiResponse<Void> processPointTransaction(
            @RequestBody @Valid PointTransactionRequest request) {
        String memberNo = securityUtils.getCurrentUserMemberNo();
        log.info("포인트 거래 요청: memberNo={}, code={}, amount={}",
                memberNo, request.getPointTransactionCode(), request.getAmount());
        pointService.processPointTransaction(memberNo, request);
        return ApiResponse.success();
    }

    /**
     * 보유 포인트 조회
     *
     * @return 보유 포인트
     */
    @Operation(summary = "보유 포인트 조회", description = "현재 사용 가능한 포인트를 조회합니다")
    @GetMapping("/balance")
    public ApiResponse<PointBalanceResponse> getPointBalance() {
        String memberNo = securityUtils.getCurrentUserMemberNo();
        log.info("보유 포인트 조회 요청: memberNo={}", memberNo);
        PointBalanceResponse response = pointService.getPointBalance(memberNo);
        return ApiResponse.success(response);
    }

    /**
     * 포인트 내역 목록 조회
     *
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 포인트 내역 목록
     */
    @Operation(summary = "포인트 내역 조회", description = "포인트 충전/사용 내역을 페이징 조회합니다")
    @GetMapping("/history")
    public ApiResponse<PointHistoryListResponse> getPointHistoryList(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") Integer size) {
        String memberNo = securityUtils.getCurrentUserMemberNo();
        log.info("포인트 내역 조회 요청: memberNo={}, page={}, size={}",
                memberNo, page, size);

        PointHistoryRequest request = new PointHistoryRequest();
        request.setPage(page);
        request.setSize(size);

        PointHistoryListResponse response = pointService.getPointHistoryList(memberNo, request);
        return ApiResponse.success(response);
    }
}
