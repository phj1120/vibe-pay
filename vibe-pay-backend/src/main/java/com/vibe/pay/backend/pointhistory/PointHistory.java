package com.vibe.pay.backend.pointhistory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PointHistory {
    private Long pointHistoryId;
    private Long memberId;
    private Long pointAmount; // 포인트 변동량 (+ 적립, - 사용)
    private Long balanceAfter; // 변동 후 잔액
    private String transactionType; // EARN(적립), USE(사용), REFUND(환불)
    private String referenceType; // PAYMENT(결제), CANCEL(취소), MANUAL(수동)
    private String referenceId; // 연관된 ID (payment_id, order_id 등)
    private String description; // 설명
    private LocalDateTime createdAt;
}