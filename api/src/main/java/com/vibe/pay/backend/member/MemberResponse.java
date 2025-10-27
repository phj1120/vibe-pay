package com.vibe.pay.backend.member;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
@Getter
@Setter
public class MemberResponse {
    private Long memberId;
    private String name;
    private String shippingAddress;
    private String phoneNumber;
    private String email;
    private LocalDateTime createdAt;
}
