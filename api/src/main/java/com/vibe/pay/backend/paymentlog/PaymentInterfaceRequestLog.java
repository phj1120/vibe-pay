package com.vibe.pay.backend.paymentlog;

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
public class PaymentInterfaceRequestLog {
    private Long logId;
    private String paymentId;
    private String requestType;
    private String requestPayload;
    private String responsePayload;
    private LocalDateTime timestamp;
}
