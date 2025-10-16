package com.vibe.pay.domain.member.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.LocalDateTime;

/**
 * 회원 엔티티
 * 사용자 정보를 관리하는 엔티티 클래스
 */
@Alias("Member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    private static final long serialVersionUID = 1234567890123456789L;

    /**
     * 회원 ID (Primary Key)
     */
    private Long memberId;

    /**
     * 회원명
     */
    private String name;

    /**
     * 배송지 주소
     */
    private String shippingAddress;

    /**
     * 전화번호
     */
    private String phoneNumber;

    /**
     * 이메일
     */
    private String email;

    /**
     * 생성일시
     */
    private LocalDateTime createdAt;
}
