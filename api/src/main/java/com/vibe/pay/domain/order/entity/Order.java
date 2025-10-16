package com.vibe.pay.domain.order.entity;

import com.vibe.pay.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 주문 엔티티
 * 주문 정보를 관리하는 엔티티 클래스
 * 복합 기본 키: (order_id, ord_seq, ord_proc_seq)
 */
@Alias("Order")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private static final long serialVersionUID = 3456789012345678901L;

    /**
     * 주문 ID (Primary Key 1)
     */
    private String orderId;

    /**
     * 주문 순번 (Primary Key 2)
     */
    private Integer ordSeq;

    /**
     * 주문 처리 순번 (Primary Key 3)
     */
    private Integer ordProcSeq;

    /**
     * 클레임 ID
     */
    private String claimId;

    /**
     * 회원 ID (Foreign Key)
     */
    private Long memberId;

    /**
     * 주문 일시
     */
    private LocalDateTime orderDate;

    /**
     * 총 주문 금액
     */
    private BigDecimal totalAmount;

    /**
     * 주문 상태
     */
    private OrderStatus status;
}
