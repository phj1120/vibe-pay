package com.vibe.pay.backend.payment;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface PaymentMapper {
    List<Payment> findAll();
    Payment findById(String id);
    void insert(Payment payment);
    void update(Payment payment);
    void delete(String id);
    
    // 결제번호 생성을 위한 시퀀스 조회
    @Select("SELECT nextval('payment_id_seq')")
    Long getNextPaymentSequence();
}