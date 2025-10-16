package com.vibe.pay.domain.payment.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.LocalDateTime;

/**
 * 결제 인터페이스 요청 로그 엔티티
 * PG사와의 통신 로그를 기록하는 엔티티 클래스
 */
@Alias("PaymentInterfaceRequestLog")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInterfaceRequestLog {
    private static final long serialVersionUID = 8901234567890123456L;

    /**
     * 로그 ID (Primary Key)
     */
    private Long logId;

    /**
     * 결제 ID (Foreign Key)
     */
    private String paymentId;

    /**
     * 요청 타입 (APPROVAL, CANCEL, REFUND 등)
     */
    private String requestType;

    /**
     * 요청 페이로드 (JSON)
     */
    private String requestPayload;

    /**
     * 응답 페이로드 (JSON)
     */
    private String responsePayload;

    /**
     * 로그 생성 일시
     */
    private LocalDateTime timestamp;
}
