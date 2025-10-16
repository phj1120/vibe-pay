package com.vibe.pay.domain.rewardpoints.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.LocalDateTime;

/**
 * 리워드 포인트 엔티티
 * 사용자별 리워드 포인트 잔액을 관리하는 엔티티 클래스
 */
@Alias("RewardPoints")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RewardPoints {
    private static final long serialVersionUID = 6789012345678901234L;

    /**
     * 리워드 포인트 ID (Primary Key)
     */
    private Long rewardPointsId;

    /**
     * 회원 ID (Foreign Key)
     */
    private Long memberId;

    /**
     * 보유 포인트
     */
    private Integer points;

    /**
     * 마지막 업데이트 일시
     */
    private LocalDateTime lastUpdated;
}
