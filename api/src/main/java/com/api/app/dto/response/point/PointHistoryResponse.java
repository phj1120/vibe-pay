package com.api.app.dto.response.point;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 포인트 내역 응답 DTO
 *
 * @author system
 * @version 1.0
 * @since 2025-10-29
 */
@Alias("PointHistoryResponse")
@Getter
@Setter
public class PointHistoryResponse implements Serializable {
    private static final long serialVersionUID = 5555666677778888999L;

    @Schema(description = "포인트기록번호")
    private String pointHistoryNo;

    @Schema(description = "금액")
    private Long amount;

    @Schema(description = "포인트적립사용코드")
    private String pointTransactionCode;

    @Schema(description = "포인트적립사용사유코드")
    private String pointTransactionReasonCode;

    @Schema(description = "포인트적립사용번호")
    private String pointTransactionReasonNo;

    @Schema(description = "시작일시")
    private LocalDateTime startDateTime;

    @Schema(description = "종료일시")
    private LocalDateTime endDateTime;

    @Schema(description = "잔여유효포인트")
    private Long remainPoint;

    @Schema(description = "등록일시")
    private LocalDateTime createdDate;
}
