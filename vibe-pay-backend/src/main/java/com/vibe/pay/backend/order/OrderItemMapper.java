package com.vibe.pay.backend.order;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface OrderItemMapper {
    List<OrderItem> findAll();
    OrderItem findById(Long id);
    List<OrderItem> findByOrderId(Long orderId);
    void insert(OrderItem orderItem);
    void update(OrderItem orderItem);
    void delete(Long id);
}
