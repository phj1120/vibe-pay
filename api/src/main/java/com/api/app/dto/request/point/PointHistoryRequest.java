package com.api.app.dto.request.point;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.io.Serializable;

/**
 * 포인트 내역 조회 요청 DTO
 *
 * @author system
 * @version 1.0
 * @since 2025-10-29
 */
@Alias("PointHistoryRequest")
@Getter
@Setter
public class PointHistoryRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 876543210987654321L;

    @Schema(description = "페이지 번호 (0부터 시작)", example = "0")
    private Integer page = 0;

    @Schema(description = "페이지 크기", example = "10")
    private Integer size = 10;

    @Schema(description = "회원번호 (서버에서 설정)")
    private String memberNo;
}
