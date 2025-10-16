package com.vibe.pay.domain.order.dto;

import com.vibe.pay.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 주문 응답 DTO
 * 주문 조회 결과 반환 시 사용
 *
 * @author claude
 * @version 1.0
 * @since 2025-10-16
 */
@Alias("OrderResponse")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private static final long serialVersionUID = 8888888888888888888L;

    /**
     * 주문 ID (Primary Key 1)
     */
    @Schema(description = "주문 ID")
    private String orderId;

    /**
     * 주문 순번 (Primary Key 2)
     */
    @Schema(description = "주문 순번")
    private Integer ordSeq;

    /**
     * 주문 처리 순번 (Primary Key 3)
     */
    @Schema(description = "주문 처리 순번")
    private Integer ordProcSeq;

    /**
     * 클레임 ID
     */
    @Schema(description = "클레임 ID")
    private String claimId;

    /**
     * 회원 ID (Foreign Key)
     */
    @Schema(description = "회원 ID")
    private Long memberId;

    /**
     * 주문 일시
     */
    @Schema(description = "주문 일시")
    private LocalDateTime orderDate;

    /**
     * 총 주문 금액
     */
    @Schema(description = "총 주문 금액")
    private BigDecimal totalAmount;

    /**
     * 주문 상태
     */
    @Schema(description = "주문 상태")
    private OrderStatus status;
}
