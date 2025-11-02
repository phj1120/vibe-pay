package com.api.app.controller.claim;

import com.api.app.common.response.ApiResponse;
import com.api.app.common.security.SecurityUtils;
import com.api.app.dto.request.claim.CancelRequest;
import com.api.app.service.claim.ClaimService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 클레임 컨트롤러
 *
 * @author Claude
 * @version 1.0
 * @since 2025-11-01
 */
@Slf4j
@RestController
@RequestMapping("/api/claim")
@RequiredArgsConstructor
@Tag(name = "클레임 관리", description = "주문 취소/반품/교환 API")
public class ClaimController {

    private final ClaimService claimService;
    private final SecurityUtils securityUtils;

    /**
     * 주문 취소
     *
     * @param request 주문 취소 요청
     * @return 성공 응답
     */
    @Operation(summary = "주문 취소", description = "주문을 취소합니다")
    @PostMapping("/cancel")
    public ApiResponse<Void> cancelOrder(@Valid @RequestBody CancelRequest request) {
        log.info("Cancel order request received. targetCount={}", request.getTargets().size());

        // 토큰에서 회원번호 추출
        String memberNo = securityUtils.getCurrentUserMemberNo();
        request.setMemberNo(memberNo);

        // 주문 취소 처리
        claimService.cancelOrder(request);

        log.info("Cancel order completed. memberNo={}", memberNo);

        return ApiResponse.success();
    }
}
