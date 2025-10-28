package com.api.app.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.LocalDateTime;

/**
 * @author system
 * @version 1.0
 * @since 2025-10-27
 */
@Alias("PointHistory")
@Getter
@Setter
public class PointHistory extends SystemEntity {
    private static final long serialVersionUID = 1123456789012345678L;

    @Schema(description = "포인트기록번호")
    private String pointHistoryNo;

    @Schema(description = "회원번호")
    private String memberNo;

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
}
