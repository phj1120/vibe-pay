package com.vibe.pay.domain.pointhistory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.LocalDateTime;

/**
 * 포인트 이력 응답 DTO
 * 포인트 이력 조회 결과 반환 시 사용
 *
 * @author claude
 * @version 1.0
 * @since 2025-10-16
 */
@Alias("PointHistoryResponse")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PointHistoryResponse {
    private static final long serialVersionUID = 1616161616161616161L;

    /**
     * 포인트 이력 ID (Primary Key)
     */
    @Schema(description = "포인트 이력 ID")
    private Long pointHistoryId;

    /**
     * 회원 ID (Foreign Key)
     */
    @Schema(description = "회원 ID")
    private Long memberId;

    /**
     * 포인트 변동 금액 (양수: 적립, 음수: 사용)
     */
    @Schema(description = "포인트 변동 금액")
    private Integer pointAmount;

    /**
     * 변동 후 잔액
     */
    @Schema(description = "변동 후 잔액")
    private Integer balanceAfter;

    /**
     * 거래 유형 (EARN, USE, REFUND 등)
     */
    @Schema(description = "거래 유형")
    private String transactionType;

    /**
     * 참조 타입 (ORDER, PAYMENT 등)
     */
    @Schema(description = "참조 타입")
    private String referenceType;

    /**
     * 참조 ID
     */
    @Schema(description = "참조 ID")
    private String referenceId;

    /**
     * 설명
     */
    @Schema(description = "설명")
    private String description;

    /**
     * 생성일시
     */
    @Schema(description = "생성일시")
    private LocalDateTime createdAt;
}
