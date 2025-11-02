package com.api.app.dto.request.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;

/**
 * 결제 취소 요청 DTO
 *
 * @author Claude
 * @version 1.0
 * @since 2025-11-02
 */
@Alias("PaymentCancelRequest")
@Getter
@Setter
@Builder
public class PaymentCancelRequest implements Serializable {
    private static final long serialVersionUID = 1234567890123456789L;

    @Schema(description = "PG사 코드", example = "001", required = true)
    @NotNull(message = "PG사 코드는 필수입니다")
    private String pgTypeCode;

    @Schema(description = "거래번호 (TID)", example = "INIpayTest20251030123456", required = true)
    @NotNull(message = "거래번호는 필수입니다")
    private String transactionId;

    @Schema(description = "주문번호", example = "20251030O000001", required = true)
    @NotNull(message = "주문번호는 필수입니다")
    private String orderNo;

    @Schema(description = "취소금액", example = "10000", required = true)
    @NotNull(message = "취소금액은 필수입니다")
    @Positive(message = "취소금액은 0보다 커야 합니다")
    private Long cancelAmount;

    @Schema(description = "취소사유", example = "고객 요청", required = true)
    @NotNull(message = "취소사유는 필수입니다")
    private String cancelReason;

    @Schema(description = "부분취소여부 (0: 전체취소, 1: 부분취소)", example = "0")
    private String partialCancelCode;
}
