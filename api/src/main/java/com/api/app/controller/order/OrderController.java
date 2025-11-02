package com.api.app.controller.order;

import com.api.app.common.response.ApiResponse;
import com.api.app.common.security.SecurityUtils;
import com.api.app.dto.request.order.OrderRequest;
import com.api.app.dto.response.order.OrderCompleteResponse;
import com.api.app.dto.response.order.OrderListResponse;
import com.api.app.dto.response.order.OrderNumberResponse;
import com.api.app.service.order.OrderService;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 주문 컨트롤러
 *
 * @author Claude
 * @version 1.0
 * @since 2025-10-31
 */
@Slf4j
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@Tag(name = "주문 관리", description = "주문 API")
public class OrderController {

    private final OrderService orderService;
    private final SecurityUtils securityUtils;

    /**
     * 주문번호 생성
     *
     * @return 생성된 주문번호
     */
    @Operation(summary = "주문번호 생성", description = "시퀀스를 이용하여 주문번호를 생성합니다")
    @GetMapping("/generateOrderNumber")
    public ApiResponse<OrderNumberResponse> generateOrderNumber() {
        log.info("Generate order number request received");

        String orderNumber = orderService.generateOrderNumber();
        OrderNumberResponse response = new OrderNumberResponse(orderNumber);

        log.info("Generate order number completed. orderNumber={}", orderNumber);

        return ApiResponse.success(response);
    }

    /**
     * 주문 생성
     *
     * @param request 주문 요청
     * @return 성공 응답
     */
    @Operation(summary = "주문 생성", description = "주문을 생성하고 결제를 처리합니다")
    @PostMapping("/order")
    public ApiResponse<Void> createOrder(@Valid @RequestBody OrderRequest request) {
        // 토큰에서 회원번호 추출하여 설정
        String memberNo = securityUtils.getCurrentUserMemberNo();
        request.setMemberNo(memberNo);

        log.info("Create order request received. memberNo={}, goodsCount={}",
                memberNo, request.getGoodsList().size());

        orderService.createOrder(request);

        log.info("Create order completed successfully");

        return ApiResponse.success();
    }

    /**
     * 주문 완료 정보 조회
     *
     * @param orderNo 주문번호
     * @return 주문 완료 정보
     */
    @Operation(summary = "주문 완료 정보 조회", description = "주문 완료 페이지에서 사용할 주문 정보를 조회합니다")
    @GetMapping("/complete/{orderNo}")
    public ApiResponse<OrderCompleteResponse> getOrderComplete(@PathVariable String orderNo) {
        // 토큰에서 회원번호 추출
        String memberNo = securityUtils.getCurrentUserMemberNo();

        log.info("Get order complete request received. orderNo={}, memberNo={}", orderNo, memberNo);

        OrderCompleteResponse response = orderService.getOrderComplete(orderNo, memberNo);

        log.info("Get order complete completed successfully. orderNo={}", orderNo);

        return ApiResponse.success(response);
    }

    /**
     * 마이페이지 주문 목록 조회
     *
     * @return 주문 목록
     */
    @Operation(summary = "주문 목록 조회", description = "마이페이지에서 사용할 주문 목록을 조회합니다")
    @GetMapping("/list")
    public ApiResponse<List<OrderListResponse>> getOrderList() {
        // 토큰에서 회원번호 추출
        String memberNo = securityUtils.getCurrentUserMemberNo();

        log.info("Get order list request received. memberNo={}", memberNo);

        List<OrderListResponse> orderList = orderService.getOrderList(memberNo);

        log.info("Get order list completed successfully. memberNo={}, count={}", memberNo, orderList.size());

        return ApiResponse.success(orderList);
    }
}
