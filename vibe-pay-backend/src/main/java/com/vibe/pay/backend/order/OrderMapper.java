package com.vibe.pay.backend.order;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface OrderMapper {
    List<Order> findAll();
    Order findById(String id);
    List<Order> findByMemberId(Long memberId);
    Order findByPaymentId(String paymentId); // New method
    void insert(Order order);
    void update(Order order);
    void delete(String id);
    
    // 주문번호 생성을 위한 시퀀스 조회
    @Select("SELECT nextval('order_id_seq')")
    Long getNextOrderSequence();
}