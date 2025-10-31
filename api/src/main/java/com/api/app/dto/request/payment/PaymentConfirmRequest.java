package com.api.app.dto.request.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * PG 승인 요청 DTO
 *
 * @author Claude
 * @version 1.0
 * @since 2025-10-31
 */
@Getter
@Setter
@Schema(description = "PG 승인 요청")
public class PaymentConfirmRequest {

    // 공통
    @Schema(description = "PG사 코드 (PAY005)", example = "001")
    private String pgTypeCode;

    @Schema(description = "인증 토큰")
    private String authToken;

    @Schema(description = "주문번호")
    private String orderNo;

    @Schema(description = "승인 URL")
    private String authUrl;

    @Schema(description = "망취소 URL")
    private String netCancelUrl;

    // 나이스 전용
    @Schema(description = "거래 ID (나이스)")
    private String transactionId;

    @Schema(description = "금액 (나이스)")
    private String amount;

    @Schema(description = "거래번호 (나이스)")
    private String tradeNo;

    @Schema(description = "가맹점 ID (나이스)")
    private String mid;

    // 이니시스 전용
    @Schema(description = "결제 금액 (이니시스)")
    private Long price;
}
