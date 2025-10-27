package com.vibe.pay.backend.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    private Long memberId;
    private String name;
    private String shippingAddress;
    private String phoneNumber;
    private String email;
    private LocalDateTime createdAt;
}
