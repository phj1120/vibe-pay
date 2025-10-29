package com.api.app.dto.response.point;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.util.List;

/**
 * 포인트 내역 목록 응답 DTO
 *
 * @author system
 * @version 1.0
 * @since 2025-10-29
 */
@Alias("PointHistoryListResponse")
@Getter
@Setter
public class PointHistoryListResponse implements Serializable {
    private static final long serialVersionUID = 2222333344445555666L;

    @Schema(description = "포인트 내역 목록")
    private List<PointHistoryResponse> content;

    @Schema(description = "현재 페이지 번호")
    private Integer page;

    @Schema(description = "페이지 크기")
    private Integer size;

    @Schema(description = "전체 개수")
    private Long totalElements;

    @Schema(description = "전체 페이지 수")
    private Integer totalPages;
}
