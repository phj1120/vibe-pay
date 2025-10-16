package com.vibe.pay.domain.order.repository;

import com.vibe.pay.domain.order.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface OrderItemMapper {
    void insert(OrderItem orderItem);
    List<OrderItem> findByOrderId(String orderId);
    List<OrderItem> findByOrderIdAndOrdProcSeq(@Param("orderId") String orderId, @Param("ordProcSeq") Integer ordProcSeq);
    List<OrderItem> findAll();
    void update(OrderItem orderItem);
    void delete(Long orderItemId);
}
