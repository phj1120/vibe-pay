package com.vibe.pay.domain.payment.dto;

import com.vibe.pay.enums.PaymentMethod;
import com.vibe.pay.enums.PgCompany;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.math.BigDecimal;

/**
 * 결제 요청 DTO
 * 결제 처리 요청 시 사용
 *
 * @author claude
 * @version 1.0
 * @since 2025-10-16
 */
@Alias("PaymentRequest")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private static final long serialVersionUID = 1212121212121212121L;

    /**
     * 회원 ID
     */
    @Schema(description = "회원 ID", example = "1")
    // @NotNull(message = "회원 ID는 필수입니다")
    private Long memberId;

    /**
     * 주문 ID
     */
    @Schema(description = "주문 ID", example = "ORD20250116001")
    // @NotBlank(message = "주문 ID는 필수입니다")
    private String orderId;

    /**
     * 결제 금액
     */
    @Schema(description = "결제 금액", example = "50000")
    // @NotNull(message = "결제 금액은 필수입니다")
    // @Min(value = 0, message = "결제 금액은 0 이상이어야 합니다")
    private BigDecimal amount;

    /**
     * 결제 수단
     */
    @Schema(description = "결제 수단", example = "CREDIT_CARD")
    // @NotNull(message = "결제 수단은 필수입니다")
    private PaymentMethod paymentMethod;

    /**
     * PG사
     */
    @Schema(description = "PG사", example = "INICIS")
    // @NotNull(message = "PG사는 필수입니다")
    private PgCompany pgCompany;
}
