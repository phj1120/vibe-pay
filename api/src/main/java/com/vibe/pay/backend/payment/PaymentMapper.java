package com.vibe.pay.backend.payment;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
@Mapper
public interface PaymentMapper {
    void insert(Payment payment);

    Payment selectByPaymentId(String paymentId);

    List<Payment> selectByOrderId(String orderId);

    List<Payment> selectAll();

    void update(Payment payment);

    void delete(Payment payment);

    Long getNextPaymentSequence();
}
