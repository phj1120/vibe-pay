package com.api.app.dto.request.goods;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;

/**
 * 상품 목록 조회 요청 DTO
 *
 * @author system
 * @version 1.0
 * @since 2025-10-29
 */
@Alias("GoodsSearchRequest")
@Getter
@Setter
public class GoodsSearchRequest implements Serializable {
    private static final long serialVersionUID = 8901234567890123456L;

    @Schema(description = "상품상태코드 (PRD001: 001-판매중/002-판매중단/003-품절)", example = "001")
    private String goodsStatusCode;

    @Schema(description = "상품명 검색어", example = "사과")
    private String goodsName;

    @Schema(description = "페이지 번호 (0부터 시작)", example = "0")
    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다")
    private Integer page = 0;

    @Schema(description = "페이지 크기", example = "20")
    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
    private Integer size = 20;
}
