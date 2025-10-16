package com.vibe.pay.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

/**
 * 회원 요청 DTO
 * 회원 생성 및 수정 요청 시 사용
 *
 * @author claude
 * @version 1.0
 * @since 2025-10-16
 */
@Alias("MemberRequest")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberRequest {
    private static final long serialVersionUID = 1111111111111111111L;

    /**
     * 회원명
     */
    @Schema(description = "회원명", example = "홍길동")
    // @NotBlank(message = "회원명은 필수입니다")
    private String name;

    /**
     * 배송지 주소
     */
    @Schema(description = "배송지 주소", example = "서울시 강남구 테헤란로 123")
    private String shippingAddress;

    /**
     * 전화번호
     */
    @Schema(description = "전화번호", example = "010-1234-5678")
    // @Pattern(regexp = "^\\d{3}-\\d{4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다")
    private String phoneNumber;

    /**
     * 이메일
     */
    @Schema(description = "이메일", example = "hong@example.com")
    // @Email(message = "이메일 형식이 올바르지 않습니다")
    private String email;
}
