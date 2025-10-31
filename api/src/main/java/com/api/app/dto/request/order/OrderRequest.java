package com.api.app.dto.request.order;

import com.api.app.dto.response.basket.BasketResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 주문 요청 DTO
 *
 * @author Claude
 * @version 1.0
 * @since 2025-10-31
 */
@Getter
@Setter
@Schema(description = "주문 요청")
public class OrderRequest {

    @Schema(description = "회원번호", example = "000000000000001")
    @NotBlank(message = "회원번호는 필수입니다")
    private String memberNo;

    @Schema(description = "회원명", example = "홍길동")
    @NotBlank(message = "회원명은 필수입니다")
    private String memberName;

    @Schema(description = "전화번호", example = "010-1234-5678")
    @NotBlank(message = "전화번호는 필수입니다")
    private String phone;

    @Schema(description = "이메일", example = "test@example.com")
    @NotBlank(message = "이메일은 필수입니다")
    private String email;

    @Schema(description = "상품 목록")
    @Valid
    @NotEmpty(message = "상품 목록은 필수입니다")
    private List<BasketResponse> goodsList;

    @Schema(description = "결제 목록")
    @Valid
    @NotEmpty(message = "결제 정보는 필수입니다")
    private List<PayRequest> payList;
}
