package com.vibe.pay.backend.payment;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface PaymentMapper {
    List<Payment> findAll();
    Payment findById(Long id);
    void insert(Payment payment);
    void update(Payment payment);
    void delete(Long id);
}