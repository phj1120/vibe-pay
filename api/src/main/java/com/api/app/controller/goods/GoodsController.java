package com.api.app.controller.goods;

import com.api.app.common.response.ApiResponse;
import com.api.app.dto.request.goods.GoodsModifyRequest;
import com.api.app.dto.request.goods.GoodsRegisterRequest;
import com.api.app.dto.request.goods.GoodsSearchRequest;
import com.api.app.dto.response.goods.GoodsDetailResponse;
import com.api.app.dto.response.goods.GoodsPageResponse;
import com.api.app.service.goods.GoodsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 상품 관리 컨트롤러
 *
 * @author system
 * @version 1.0
 * @since 2025-10-29
 */
@Slf4j
@RestController
@RequestMapping("/api/goods")
@RequiredArgsConstructor
@Tag(name = "상품 관리", description = "상품 등록, 수정, 조회, 삭제 API")
public class GoodsController {

    private final GoodsService goodsService;

    /**
     * 상품 등록
     *
     * @param request 상품 등록 요청
     * @return 생성된 상품번호
     */
    @Operation(summary = "상품 등록", description = "신규 상품을 등록합니다")
    @PostMapping
    public ApiResponse<String> registerGoods(@RequestBody @Valid GoodsRegisterRequest request) {
        log.info("상품 등록 요청: goodsName={}", request.getGoodsName());
        String goodsNo = goodsService.registerGoods(request);
        return ApiResponse.success(goodsNo);
    }

    /**
     * 상품 수정
     *
     * @param goodsNo 상품번호
     * @param request 상품 수정 요청
     * @return 성공 응답
     */
    @Operation(summary = "상품 수정", description = "상품 정보를 수정합니다")
    @PutMapping("/{goodsNo}")
    public ApiResponse<Void> modifyGoods(
            @Parameter(description = "상품번호", required = true, example = "G00000000000001")
            @PathVariable String goodsNo,
            @RequestBody @Valid GoodsModifyRequest request) {
        log.info("상품 수정 요청: goodsNo={}", goodsNo);
        goodsService.modifyGoods(goodsNo, request);
        return ApiResponse.success();
    }

    /**
     * 상품 목록 조회 (페이징)
     *
     * @param request 검색 조건
     * @return 상품 목록
     */
    @Operation(summary = "상품 목록 조회", description = "상품 목록을 조회합니다 (페이징 지원)")
    @GetMapping
    public ApiResponse<GoodsPageResponse> getGoodsList(@ModelAttribute @Valid GoodsSearchRequest request) {
        log.info("상품 목록 조회 요청: page={}, size={}", request.getPage(), request.getSize());
        GoodsPageResponse response = goodsService.getGoodsList(request);
        return ApiResponse.success(response);
    }

    /**
     * 상품 상세 조회
     *
     * @param goodsNo 상품번호
     * @return 상품 상세
     */
    @Operation(summary = "상품 상세 조회", description = "상품 상세 정보를 조회합니다")
    @GetMapping("/{goodsNo}")
    public ApiResponse<GoodsDetailResponse> getGoodsDetail(
            @Parameter(description = "상품번호", required = true, example = "G00000000000001")
            @PathVariable String goodsNo) {
        log.info("상품 상세 조회 요청: goodsNo={}", goodsNo);
        GoodsDetailResponse response = goodsService.getGoodsDetail(goodsNo);
        return ApiResponse.success(response);
    }

    /**
     * 상품 삭제
     *
     * @param goodsNo 상품번호
     * @return 성공 응답
     */
    @Operation(summary = "상품 삭제", description = "상품을 삭제합니다")
    @DeleteMapping("/{goodsNo}")
    public ApiResponse<Void> deleteGoods(
            @Parameter(description = "상품번호", required = true, example = "G00000000000001")
            @PathVariable String goodsNo) {
        log.info("상품 삭제 요청: goodsNo={}", goodsNo);
        goodsService.deleteGoods(goodsNo);
        return ApiResponse.success();
    }
}
