package com.vibe.pay.domain.rewardpoints.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.LocalDateTime;

/**
 * 리워드 포인트 응답 DTO
 * 포인트 조회 결과 반환 시 사용
 *
 * @author claude
 * @version 1.0
 * @since 2025-10-16
 */
@Alias("RewardPointsResponse")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RewardPointsResponse {
    private static final long serialVersionUID = 1515151515151515151L;

    /**
     * 리워드 포인트 ID (Primary Key)
     */
    @Schema(description = "리워드 포인트 ID")
    private Long rewardPointsId;

    /**
     * 회원 ID (Foreign Key)
     */
    @Schema(description = "회원 ID")
    private Long memberId;

    /**
     * 보유 포인트
     */
    @Schema(description = "보유 포인트")
    private Integer points;

    /**
     * 마지막 업데이트 일시
     */
    @Schema(description = "마지막 업데이트 일시")
    private LocalDateTime lastUpdated;
}
