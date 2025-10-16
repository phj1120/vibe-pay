package com.vibe.pay.domain.payment.entity;

import com.vibe.pay.enums.OrderStatus;
import com.vibe.pay.enums.PayType;
import com.vibe.pay.enums.PaymentMethod;
import com.vibe.pay.enums.PgCompany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 결제 엔티티
 * 결제 정보를 관리하는 엔티티 클래스
 * 복합 기본 키: (payment_id, payment_method, order_id, pay_type)
 */
@Alias("Payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    private static final long serialVersionUID = 5678901234567890123L;

    /**
     * 결제 ID (Primary Key 1)
     */
    private String paymentId;

    /**
     * 결제 수단 (Primary Key 2)
     */
    private PaymentMethod paymentMethod;

    /**
     * 주문 ID (Primary Key 3)
     */
    private String orderId;

    /**
     * 결제 타입 (Primary Key 4)
     */
    private PayType payType;

    /**
     * 회원 ID (Foreign Key)
     */
    private Long memberId;

    /**
     * 클레임 ID
     */
    private String claimId;

    /**
     * 결제 금액
     */
    private BigDecimal amount;

    /**
     * PG사
     */
    private PgCompany pgCompany;

    /**
     * 결제 상태
     */
    private String status;

    /**
     * 주문 상태
     */
    private OrderStatus orderStatus;

    /**
     * 거래 ID (PG사에서 발급)
     */
    private String transactionId;

    /**
     * 승인 번호 (PG사에서 발급)
     */
    private String approvalNumber;

    /**
     * 결제 일시
     */
    private LocalDateTime paymentDate;
}
