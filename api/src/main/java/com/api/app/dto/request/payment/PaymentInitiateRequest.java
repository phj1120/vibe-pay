package com.api.app.dto.request.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 결제 초기화 요청 DTO
 *
 * @author Claude
 * @version 1.0
 * @since 2025-10-31
 */
@Getter
@Setter
@Schema(description = "결제 초기화 요청")
public class PaymentInitiateRequest {

    @Schema(description = "주문번호", example = "20251031O000001")
    @NotBlank(message = "주문번호는 필수입니다")
    private String orderNumber;

    @Schema(description = "결제수단", example = "CARD")
    private String paymentMethod;

    @Schema(description = "PG사 타입", example = "INICIS")
    private String pgType;

    @Schema(description = "결제금액", example = "50000")
    @NotNull(message = "결제금액은 필수입니다")
    private Long amount;

    @Schema(description = "상품명", example = "상품A 외 1건")
    @NotBlank(message = "상품명은 필수입니다")
    private String productName;

    @Schema(description = "구매자명", example = "홍길동")
    @NotBlank(message = "구매자명은 필수입니다")
    private String buyerName;

    @Schema(description = "구매자 이메일", example = "test@example.com")
    @NotBlank(message = "이메일은 필수입니다")
    private String buyerEmail;

    @Schema(description = "구매자 전화번호", example = "010-1234-5678")
    @NotBlank(message = "전화번호는 필수입니다")
    private String buyerTel;
}
