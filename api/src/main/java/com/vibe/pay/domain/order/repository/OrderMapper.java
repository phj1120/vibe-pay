package com.vibe.pay.domain.order.repository;

import com.vibe.pay.domain.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface OrderMapper {
    void insert(Order order);
    List<Order> findByOrderId(String orderId);
    List<Order> findByMemberId(Long memberId);
    List<Order> findByOrderIdAndOrdProcSeq(@Param("orderId") String orderId, @Param("ordProcSeq") Integer ordProcSeq);
    List<Order> getOrderDetailsWithPaymentsByMemberId(@Param("memberId") Long memberId);
    List<Order> findAll();
    void update(Order order);
    void delete(@Param("orderId") String orderId, @Param("ordSeq") Integer ordSeq, @Param("ordProcSeq") Integer ordProcSeq);
    Long getNextOrderSequence();
    Long getNextClaimSequence();
}
