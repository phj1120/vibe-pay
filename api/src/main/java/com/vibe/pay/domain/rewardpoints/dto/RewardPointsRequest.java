package com.vibe.pay.domain.rewardpoints.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

/**
 * 리워드 포인트 요청 DTO
 * 포인트 적립/사용 요청 시 사용
 *
 * @author claude
 * @version 1.0
 * @since 2025-10-16
 */
@Alias("RewardPointsRequest")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RewardPointsRequest {
    private static final long serialVersionUID = 1414141414141414141L;

    /**
     * 회원 ID
     */
    @Schema(description = "회원 ID", example = "1")
    // @NotNull(message = "회원 ID는 필수입니다")
    private Long memberId;

    /**
     * 포인트 (양수: 적립, 음수: 사용)
     */
    @Schema(description = "포인트", example = "1000")
    // @NotNull(message = "포인트는 필수입니다")
    private Long points;
}
