package com.vibe.pay.backend.order;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
@Mapper
public interface OrderItemMapper {
    void insert(OrderItem orderItem);

    OrderItem selectById(Long orderItemId);

    List<OrderItem> selectByOrderId(String orderId);

    List<OrderItem> selectByOrderIdAndOrdProcSeq(@Param("orderId") String orderId, @Param("ordProcSeq") Integer ordProcSeq);

    List<OrderItem> selectAll();

    void update(OrderItem orderItem);

    void delete(Long orderItemId);
}
