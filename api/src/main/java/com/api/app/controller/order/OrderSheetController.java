package com.api.app.controller.order;

import com.api.app.common.response.ApiResponse;
import com.api.app.dto.response.order.OrderSheetResponse;
import com.api.app.service.order.OrderSheetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 주문 관리 컨트롤러
 *
 * @author system
 * @version 1.0
 * @since 2025-10-30
 */
@Slf4j
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@Tag(name = "주문 관리", description = "주문서 조회 및 주문 처리 API")
public class OrderSheetController {

    private final OrderSheetService orderSheetService;

    /**
     * 주문서 정보 조회
     *
     * @param userDetails 인증된 사용자 정보
     * @param basketNos   장바구니 번호 목록
     * @return 주문서 정보
     */
    @Operation(summary = "주문서 정보 조회", description = "장바구니 번호 목록으로 주문서 정보를 조회합니다")
    @GetMapping("/sheet")
    public ApiResponse<OrderSheetResponse> getOrderSheet(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "장바구니 번호 목록 (콤마 구분)", example = "B001,B002", required = true)
            @RequestParam List<String> basketNos) {

        log.info("주문서 조회 요청: email={}, basketNos={}", userDetails.getUsername(), basketNos);
        OrderSheetResponse response = orderSheetService.getOrderSheet(userDetails.getUsername(), basketNos);
        return ApiResponse.success(response);
    }
}
