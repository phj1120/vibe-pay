package com.vibe.pay.domain.order.dto;

import com.vibe.pay.domain.order.entity.Order;
import com.vibe.pay.domain.payment.entity.Payment;
import com.vibe.pay.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 주문 상세 DTO
 * 주문 상세 정보 조회 시 사용 (주문 + 주문상품 + 결제 + 주문처리 이력)
 *
 * @author claude
 * @version 1.0
 * @since 2025-10-16
 */
@Alias("OrderDetailDto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailDto {
    private static final long serialVersionUID = 1010101010101010101L;

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

    /**
     * 주문 상품 목록
     */
    @Schema(description = "주문 상품 목록")
    private List<OrderItemDto> orderItems;

    /**
     * 결제 목록
     */
    @Schema(description = "결제 목록")
    private List<Payment> payments;

    /**
     * 주문 처리 이력 목록
     */
    @Schema(description = "주문 처리 이력 목록")
    private List<Order> orderProcesses;
}
