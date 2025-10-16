package com.vibe.pay.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.LocalDateTime;

/**
 * 회원 응답 DTO
 * 회원 조회 결과 반환 시 사용
 *
 * @author claude
 * @version 1.0
 * @since 2025-10-16
 */
@Alias("MemberResponse")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponse {
    private static final long serialVersionUID = 2222222222222222222L;

    /**
     * 회원 ID (Primary Key)
     */
    @Schema(description = "회원 ID")
    private Long memberId;

    /**
     * 회원명
     */
    @Schema(description = "회원명")
    private String name;

    /**
     * 배송지 주소
     */
    @Schema(description = "배송지 주소")
    private String shippingAddress;

    /**
     * 전화번호
     */
    @Schema(description = "전화번호")
    private String phoneNumber;

    /**
     * 이메일
     */
    @Schema(description = "이메일")
    private String email;

    /**
     * 생성일시
     */
    @Schema(description = "생성일시")
    private LocalDateTime createdAt;
}
