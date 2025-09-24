package com.vibe.pay.backend.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    private String paymentId;
    private Long memberId;
    private String orderId;
    private String claimId;
    private Long amount;
    private String paymentMethod;
    private String payType; // PAYMENT(결제), REFUND(환불)
    private String pgCompany;
    private String status;
    private String orderStatus; // ORDER(주문), CANCELED(취소)
    private String transactionId;
    private LocalDateTime paymentDate;
}
