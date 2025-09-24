package com.vibe.pay.backend.payment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentConfirmRequest {
    private String status; // e.g., "SUCCESS", "FAILURE"
    private String transactionId;
    
    // 이니시스 승인 처리용 필드
    private String authToken;
    private String authUrl;
    private String netCancelUrl;
    private String mid;
    private String orderId; // 이니시스에서 전달되는 실제 필드명 (oid와 동일)
    private Long price;
    
    // 결제 승인 시 필요한 추가 정보
    private Long memberId;
    private String paymentId;
    private String paymentMethod;
}
