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
 * 결제 수단 요청 DTO
 * 주문 생성 시 사용할 결제 수단 정보
 *
 * @author claude
 * @version 1.0
 * @since 2025-10-16
 */
@Alias("PaymentMethodRequest")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodRequest {
    private static final long serialVersionUID = 6666666666666666666L;

    /**
     * 결제 수단
     */
    @Schema(description = "결제 수단", example = "CREDIT_CARD")
    // @NotNull(message = "결제 수단은 필수입니다")
    private PaymentMethod paymentMethod;

    /**
     * 결제 금액
     */
    @Schema(description = "결제 금액", example = "50000")
    // @NotNull(message = "결제 금액은 필수입니다")
    // @Min(value = 0, message = "결제 금액은 0 이상이어야 합니다")
    private BigDecimal amount;

    /**
     * 인증 토큰
     */
    @Schema(description = "인증 토큰")
    private String authToken;

    /**
     * 인증 URL
     */
    @Schema(description = "인증 URL")
    private String authUrl;

    /**
     * 가맹점 ID
     */
    @Schema(description = "가맹점 ID")
    private String mid;

    /**
     * 망 취소 URL
     */
    @Schema(description = "망 취소 URL")
    private String netCancelUrl;

    /**
     * PG사
     */
    @Schema(description = "PG사", example = "INICIS")
    private PgCompany pgCompany;

    /**
     * 거래 ID
     */
    @Schema(description = "거래 ID")
    private String txTid;

    /**
     * 다음 앱 URL
     */
    @Schema(description = "다음 앱 URL")
    private String nextAppUrl;
}
