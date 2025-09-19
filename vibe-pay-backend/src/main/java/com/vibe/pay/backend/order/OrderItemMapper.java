package com.vibe.pay.backend.order;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface OrderItemMapper {
    List<OrderItem> findAll();
    OrderItem findByOrderItemId(Long orderItemId);
    List<OrderItem> findByOrderId(String orderId);
    List<OrderItem> findByOrderIdAndClaimId(@Param("orderId") String orderId, @Param("claimId") String claimId);
    void insert(OrderItem orderItem);
    void update(OrderItem orderItem);
    void delete(Long orderItemId);
}
