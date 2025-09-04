package com.vibe.pay.backend.order;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface OrderMapper {
    List<Order> findAll();
    Order findById(Long id);
    List<Order> findByMemberId(Long memberId);
    Order findByPaymentId(Long paymentId); // New method
    void insert(Order order);
    void update(Order order);
    void delete(Long id);
}