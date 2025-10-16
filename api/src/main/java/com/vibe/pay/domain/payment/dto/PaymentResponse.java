package com.vibe.pay.domain.payment.dto;

import com.vibe.pay.enums.OrderStatus;
import com.vibe.pay.enums.PayType;
import com.vibe.pay.enums.PaymentMethod;
import com.vibe.pay.enums.PgCompany;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 결제 응답 DTO
 * 결제 조회 결과 반환 시 사용
 *
 * @author claude
 * @version 1.0
 * @since 2025-10-16
 */
@Alias("PaymentResponse")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private static final long serialVersionUID = 1313131313131313131L;

    /**
     * 결제 ID (Primary Key 1)
     */
    @Schema(description = "결제 ID")
    private String paymentId;

    /**
     * 결제 수단 (Primary Key 2)
     */
    @Schema(description = "결제 수단")
    private PaymentMethod paymentMethod;

    /**
     * 주문 ID (Primary Key 3)
     */
    @Schema(description = "주문 ID")
    private String orderId;

    /**
     * 결제 타입 (Primary Key 4)
     */
    @Schema(description = "결제 타입")
    private PayType payType;

    /**
     * 회원 ID (Foreign Key)
     */
    @Schema(description = "회원 ID")
    private Long memberId;

    /**
     * 클레임 ID
     */
    @Schema(description = "클레임 ID")
    private String claimId;

    /**
     * 결제 금액
     */
    @Schema(description = "결제 금액")
    private BigDecimal amount;

    /**
     * PG사
     */
    @Schema(description = "PG사")
    private PgCompany pgCompany;

    /**
     * 결제 상태
     */
    @Schema(description = "결제 상태")
    private String status;

    /**
     * 주문 상태
     */
    @Schema(description = "주문 상태")
    private OrderStatus orderStatus;

    /**
     * 거래 ID (PG사에서 발급)
     */
    @Schema(description = "거래 ID")
    private String transactionId;

    /**
     * 결제 일시
     */
    @Schema(description = "결제 일시")
    private LocalDateTime paymentDate;
}
