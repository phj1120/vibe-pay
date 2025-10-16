package com.vibe.pay.domain.payment.repository;

import com.vibe.pay.domain.payment.entity.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Optional;

@Mapper
public interface PaymentMapper {
    void insert(Payment payment);
    Optional<Payment> findByPaymentId(String paymentId);
    List<Payment> findByOrderId(String orderId);
    List<Payment> findByMemberId(Long memberId);
    List<Payment> findAll();
    void update(Payment payment);
    void delete(String paymentId);
}
