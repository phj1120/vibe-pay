package com.vibe.pay.backend.member;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
@Getter
@Setter
public class MemberRequest {
    private String name;
    private String shippingAddress;
    private String phoneNumber;
    private String email;
}
