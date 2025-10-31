package com.api.app.dto.response.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * PG 승인 응답 DTO
 *
 * @author Claude
 * @version 1.0
 * @since 2025-10-31
 */
@Getter
@Builder
@Schema(description = "PG 승인 응답")
public class PaymentApprovalResponse {

    @Schema(description = "승인번호")
    private String approveNo;

    @Schema(description = "거래ID")
    private String trdNo;

    @Schema(description = "결제금액")
    private Long amount;

    @Schema(description = "카드번호")
    private String cardNo;

    @Schema(description = "카드사 코드")
    private String cardCode;
}
