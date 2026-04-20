package com.api.app.dto.request.order

import com.api.app.client.dto.BasketResponse
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class OrderRequest(
    @field:NotBlank(message = "주문번호는 필수입니다")
    val orderNo: String,

    val memberNo: String? = null,

    @field:NotBlank(message = "회원명은 필수입니다")
    val memberName: String,

    @field:NotBlank(message = "전화번호는 필수입니다")
    val phone: String,

    @field:NotBlank(message = "이메일은 필수입니다")
    val email: String,

    @field:Valid
    @field:NotEmpty(message = "상품 목록은 필수입니다")
    val goodsList: List<BasketResponse>,

    @field:Valid
    @field:NotEmpty(message = "결제 정보는 필수입니다")
    val payList: List<PayRequest>
)
