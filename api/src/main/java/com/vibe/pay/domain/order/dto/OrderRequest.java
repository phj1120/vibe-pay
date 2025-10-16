package com.vibe.pay.domain.order.dto;

import com.vibe.pay.domain.payment.dto.PaymentMethodRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.util.List;

/**
 * 주문 요청 DTO
 * 주문 생성 요청 시 사용
 *
 * @author claude
 * @version 1.0
 * @since 2025-10-16
 */
@Alias("OrderRequest")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    private static final long serialVersionUID = 7777777777777777777L;

    /**
     * 주문 번호
     */
    @Schema(description = "주문 번호", example = "ORD20250116001")
    // @NotBlank(message = "주문 번호는 필수입니다")
    private String orderNumber;

    /**
     * 회원 ID
     */
    @Schema(description = "회원 ID", example = "1")
    // @NotNull(message = "회원 ID는 필수입니다")
    private Long memberId;

    /**
     * 주문 상품 목록
     */
    @Schema(description = "주문 상품 목록")
    // @NotEmpty(message = "주문 상품은 최소 1개 이상이어야 합니다")
    private List<OrderItemRequest> items;

    /**
     * 결제 수단 목록
     */
    @Schema(description = "결제 수단 목록")
    // @NotEmpty(message = "결제 수단은 최소 1개 이상이어야 합니다")
    private List<PaymentMethodRequest> paymentMethods;

    /**
     * 망 취소 여부
     */
    @Schema(description = "망 취소 여부", example = "false")
    private Boolean netCancel;
}
