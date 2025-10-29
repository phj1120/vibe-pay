package com.api.app.controller.basket;

import com.api.app.common.response.ApiResponse;
import com.api.app.dto.request.basket.BasketAddRequest;
import com.api.app.dto.request.basket.BasketModifyRequest;
import com.api.app.dto.response.basket.BasketResponse;
import com.api.app.service.basket.BasketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 장바구니 관리 컨트롤러
 *
 * @author system
 * @version 1.0
 * @since 2025-10-29
 */
@Slf4j
@RestController
@RequestMapping("/api/baskets")
@RequiredArgsConstructor
@Tag(name = "장바구니 관리", description = "장바구니 추가, 수정, 조회, 삭제 API")
public class BasketController {

    private final BasketService basketService;

    /**
     * 장바구니 목록 조회
     *
     * @param userDetails 인증된 사용자 정보
     * @return 장바구니 목록
     */
    @Operation(summary = "장바구니 목록 조회", description = "현재 로그인한 회원의 장바구니 목록을 조회합니다")
    @GetMapping
    public ApiResponse<List<BasketResponse>> getBasketList(
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("장바구니 목록 조회 요청: email={}", userDetails.getUsername());
        List<BasketResponse> response = basketService.getBasketList(userDetails.getUsername());
        return ApiResponse.success(response);
    }

    /**
     * 장바구니 추가
     *
     * @param userDetails 인증된 사용자 정보
     * @param request     장바구니 추가 요청
     * @return 생성된 장바구니번호
     */
    @Operation(summary = "장바구니 추가",
            description = "장바구니에 상품을 추가합니다. 동일한 상품/단품이 이미 있으면 수량이 증가합니다")
    @PostMapping
    public ApiResponse<String> addBasket(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid BasketAddRequest request) {
        log.info("장바구니 추가 요청: email={}, goodsNo={}, itemNo={}",
                userDetails.getUsername(), request.getGoodsNo(), request.getItemNo());
        String basketNo = basketService.addBasket(userDetails.getUsername(), request);
        return ApiResponse.success(basketNo);
    }

    /**
     * 장바구니 수정
     *
     * @param userDetails 인증된 사용자 정보
     * @param basketNo    장바구니번호
     * @param request     장바구니 수정 요청
     * @return 성공 응답
     */
    @Operation(summary = "장바구니 수정", description = "장바구니 정보를 수정합니다")
    @PutMapping("/{basketNo}")
    public ApiResponse<Void> modifyBasket(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "장바구니번호", required = true, example = "000000000000001")
            @PathVariable String basketNo,
            @RequestBody @Valid BasketModifyRequest request) {
        log.info("장바구니 수정 요청: email={}, basketNo={}",
                userDetails.getUsername(), basketNo);
        basketService.modifyBasket(userDetails.getUsername(), basketNo, request);
        return ApiResponse.success();
    }

    /**
     * 장바구니 삭제
     *
     * @param userDetails 인증된 사용자 정보
     * @param basketNo    장바구니번호
     * @return 성공 응답
     */
    @Operation(summary = "장바구니 삭제", description = "장바구니 항목을 삭제합니다")
    @DeleteMapping("/{basketNo}")
    public ApiResponse<Void> deleteBasket(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "장바구니번호", required = true, example = "000000000000001")
            @PathVariable String basketNo) {
        log.info("장바구니 삭제 요청: email={}, basketNo={}",
                userDetails.getUsername(), basketNo);
        basketService.deleteBasket(userDetails.getUsername(), basketNo);
        return ApiResponse.success();
    }

    /**
     * 장바구니 여러 개 삭제
     *
     * @param userDetails 인증된 사용자 정보
     * @param basketNos   장바구니번호 목록
     * @return 성공 응답
     */
    @Operation(summary = "장바구니 여러 개 삭제", description = "선택한 장바구니 항목들을 삭제합니다")
    @DeleteMapping
    public ApiResponse<Void> deleteBaskets(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "장바구니번호 목록", required = true)
            @RequestParam List<String> basketNos) {
        log.info("장바구니 여러 개 삭제 요청: email={}, count={}",
                userDetails.getUsername(), basketNos.size());
        basketService.deleteBaskets(userDetails.getUsername(), basketNos);
        return ApiResponse.success();
    }

    /**
     * 장바구니 전체 삭제
     *
     * @param userDetails 인증된 사용자 정보
     * @return 성공 응답
     */
    @Operation(summary = "장바구니 전체 삭제", description = "회원의 모든 장바구니 항목을 삭제합니다")
    @DeleteMapping("/all")
    public ApiResponse<Void> deleteAllBaskets(
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("장바구니 전체 삭제 요청: email={}", userDetails.getUsername());
        basketService.deleteAllBaskets(userDetails.getUsername());
        return ApiResponse.success();
    }
}
