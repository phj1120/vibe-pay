package com.vibe.pay.backend.order;

import com.vibe.pay.backend.payment.Payment;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
@Getter
@Setter
public class OrderDetailDto {
    private String orderId;
    private Integer ordSeq;
    private Integer ordProcSeq;
    private Long memberId;
    private LocalDateTime orderDate;
    private Double totalAmount;
    private String status;
    private List<Order> orderProcesses;
    private List<OrderItemDto> orderItems;
    private List<Payment> payments;
}
