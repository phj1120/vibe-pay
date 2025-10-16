package com.vibe.pay.domain.pointhistory.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.LocalDateTime;

/**
 * 포인트 이력 엔티티
 * 리워드 포인트 적립/사용 내역을 관리하는 엔티티 클래스
 */
@Alias("PointHistory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PointHistory {
    private static final long serialVersionUID = 7890123456789012345L;

    /**
     * 포인트 이력 ID (Primary Key)
     */
    private Long pointHistoryId;

    /**
     * 회원 ID (Foreign Key)
     */
    private Long memberId;

    /**
     * 포인트 변동 금액 (양수: 적립, 음수: 사용)
     */
    private Long pointAmount;

    /**
     * 변동 후 잔액
     */
    private Long balanceAfter;

    /**
     * 거래 유형 (EARN, USE, REFUND 등)
     */
    private String transactionType;

    /**
     * 참조 타입 (ORDER, PAYMENT 등)
     */
    private String referenceType;

    /**
     * 참조 ID
     */
    private String referenceId;

    /**
     * 설명
     */
    private String description;

    /**
     * 생성일시
     */
    private LocalDateTime createdAt;
}
