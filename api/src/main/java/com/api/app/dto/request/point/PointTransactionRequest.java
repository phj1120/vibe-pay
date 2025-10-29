package com.api.app.dto.request.point;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;

/**
 * 포인트 충전/사용 요청 DTO
 *
 * @author system
 * @version 1.0
 * @since 2025-10-29
 */
@Alias("PointTransactionRequest")
@Getter
@Setter
public class PointTransactionRequest implements Serializable {
    private static final long serialVersionUID = 1234567890123456789L;

    @Schema(description = "금액", example = "1000", required = true)
    @NotNull(message = "금액은 필수입니다")
    @Positive(message = "금액은 0보다 커야 합니다")
    private Long amount;

    @Schema(description = "포인트적립사용코드 (001: 적립, 002: 사용)", example = "001", required = true)
    @NotNull(message = "포인트적립사용코드는 필수입니다")
    @Pattern(regexp = "^(001|002)$", message = "포인트적립사용코드는 001(적립) 또는 002(사용)만 가능합니다")
    private String pointTransactionCode;

    @Schema(description = "포인트적립사용사유코드 (001: 기타, 002: 주문)", example = "001", required = true)
    @NotNull(message = "포인트적립사용사유코드는 필수입니다")
    @Pattern(regexp = "^(001|002)$", message = "포인트적립사용사유코드는 001(기타) 또는 002(주문)만 가능합니다")
    private String pointTransactionReasonCode;

    @Schema(description = "포인트적립사용번호 (주문번호 등)", example = "")
    private String pointTransactionReasonNo;
}
