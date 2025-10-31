package com.api.app.dto.response.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 주문번호 생성 응답 DTO
 *
 * @author Claude
 * @version 1.0
 * @since 2025-10-31
 */
@Getter
@AllArgsConstructor
@Schema(description = "주문번호 생성 응답")
public class OrderNumberResponse {

    @Schema(description = "주문번호", example = "20251031O000001")
    private String orderNumber;
}
