package com.vibe.pay.domain.payment.controller;

import com.vibe.pay.domain.payment.dto.PaymentResponse;
import com.vibe.pay.domain.payment.entity.Payment;
import com.vibe.pay.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 결제 관리 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByOrderId(@PathVariable String orderId) {
        log.info("Getting payments by order ID: {}", orderId);
        List<Payment> payments = paymentService.findByOrderId(orderId);
        return ResponseEntity.ok(payments.stream().map(this::toResponse).collect(Collectors.toList()));
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByMemberId(@PathVariable Long memberId) {
        log.info("Getting payments by member ID: {}", memberId);
        List<Payment> payments = paymentService.findByMemberId(memberId);
        return ResponseEntity.ok(payments.stream().map(this::toResponse).collect(Collectors.toList()));
    }

    private PaymentResponse toResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(payment.getPaymentId());
        response.setMemberId(payment.getMemberId());
        response.setOrderId(payment.getOrderId());
        response.setClaimId(payment.getClaimId());
        response.setAmount(payment.getAmount());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setPayType(payment.getPayType());
        response.setPgCompany(payment.getPgCompany());
        response.setStatus(payment.getStatus());
        response.setOrderStatus(payment.getOrderStatus());
        response.setTransactionId(payment.getTransactionId());
        response.setPaymentDate(payment.getPaymentDate());
        return response;
    }
}