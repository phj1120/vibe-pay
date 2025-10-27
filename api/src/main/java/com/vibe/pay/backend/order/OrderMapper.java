package com.vibe.pay.backend.order;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
@Mapper
public interface OrderMapper {
    void insert(Order order);

    List<Order> selectByOrderId(String orderId);

    List<Order> selectByMemberId(Long memberId);

    List<Order> selectAll();

    void update(Order order);

    void delete(String orderId);

    Long getNextOrderSequence();

    Long getNextClaimSequence();
}
