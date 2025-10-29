package com.api.app.dto.response.goods;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.util.List;

/**
 * 상품 페이징 응답 DTO
 *
 * @author system
 * @version 1.0
 * @since 2025-10-29
 */
@Alias("GoodsPageResponse")
@Getter
@Setter
public class GoodsPageResponse implements Serializable {
    private static final long serialVersionUID = 3344556677889900112L;

    @Schema(description = "상품 목록")
    private List<GoodsListResponse> content;

    @Schema(description = "현재 페이지 번호")
    private Integer page;

    @Schema(description = "페이지 크기")
    private Integer size;

    @Schema(description = "전체 데이터 개수")
    private Long totalElements;

    @Schema(description = "전체 페이지 개수")
    private Integer totalPages;

    @Schema(description = "마지막 페이지 여부")
    private Boolean last;
}
