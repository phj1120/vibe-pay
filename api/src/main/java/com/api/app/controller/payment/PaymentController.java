package com.api.app.controller.payment;

import com.api.app.common.response.ApiResponse;
import com.api.app.dto.request.payment.PaymentInitiateRequest;
import com.api.app.dto.response.payment.PaymentInitiateResponse;
import com.api.app.service.payment.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * 결제 컨트롤러
 *
 * @author Claude
 * @version 1.0
 * @since 2025-10-31
 */
@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "결제 관리", description = "결제 API")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 결제 초기화
     *
     * @param request 결제 초기화 요청
     * @return 결제 초기화 응답
     */
    @Operation(summary = "결제 초기화", description = "PG사별 결제 정보를 생성합니다")
    @PostMapping("/initiate")
    public ApiResponse<PaymentInitiateResponse> initiatePayment(
            @Valid @RequestBody PaymentInitiateRequest request) {
        log.info("Payment initiate request received. orderNumber={}, amount={}",
                request.getOrderNumber(), request.getAmount());

        PaymentInitiateResponse response = paymentService.initiatePayment(request);

        log.info("Payment initiate completed. pgType={}", response.getPgType());

        return ApiResponse.success(response);
    }
}
