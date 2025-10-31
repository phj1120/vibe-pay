package com.api.app.dto.request.order;

import com.api.app.dto.request.payment.PaymentConfirmRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 결제 요청 DTO (주문 내 결제 정보)
 *
 * @author Claude
 * @version 1.0
 * @since 2025-10-31
 */
@Getter
@Setter
@Schema(description = "결제 요청")
public class PayRequest {

    @Schema(description = "결제방식코드 (PAY002)", example = "001")
    @NotBlank(message = "결제방식코드는 필수입니다")
    private String payWayCode;

    @Schema(description = "결제금액", example = "50000")
    @NotNull(message = "결제금액은 필수입니다")
    private Long amount;

    @Schema(description = "결제유형코드 (PAY001)", example = "001")
    @NotBlank(message = "결제유형코드는 필수입니다")
    private String payTypeCode;

    @Schema(description = "PG 승인 요청 데이터")
    @Valid
    private PaymentConfirmRequest paymentConfirmRequest;
}
