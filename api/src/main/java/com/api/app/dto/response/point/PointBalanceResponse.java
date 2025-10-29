package com.api.app.dto.response.point;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;

/**
 * 포인트 잔액 조회 응답 DTO
 *
 * @author system
 * @version 1.0
 * @since 2025-10-29
 */
@Alias("PointBalanceResponse")
@Getter
@Setter
public class PointBalanceResponse implements Serializable {
    private static final long serialVersionUID = 1111222233334444555L;

    @Schema(description = "보유 포인트")
    private Long totalPoint;
}
