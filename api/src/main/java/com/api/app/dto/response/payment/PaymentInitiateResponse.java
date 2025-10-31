package com.api.app.dto.response.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

/**
 * 결제 초기화 응답 DTO
 *
 * @author Claude
 * @version 1.0
 * @since 2025-10-31
 */
@Getter
@Builder
@Schema(description = "결제 초기화 응답")
public class PaymentInitiateResponse {

    @Schema(description = "PG사 타입", example = "INICIS")
    private String pgType;

    @Schema(description = "PG사 코드 (PAY005)", example = "001")
    private String pgTypeCode;

    @Schema(description = "결제수단", example = "CARD")
    private String paymentMethod;

    @Schema(description = "가맹점 ID", example = "INIpayTest")
    private String merchantId;

    @Schema(description = "가맹점 키")
    private String merchantKey;

    @Schema(description = "리턴 URL", example = "http://localhost:3000/order/return")
    private String returnUrl;

    @Schema(description = "PG사별 Form 데이터")
    private Map<String, String> formData;
}
