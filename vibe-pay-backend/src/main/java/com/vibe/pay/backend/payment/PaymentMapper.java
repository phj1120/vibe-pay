package com.vibe.pay.backend.payment;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface PaymentMapper {
    List<Payment> findAll();
    Payment findByPaymentId(String paymentId);
    Payment findByOrderId(String orderId);
    void insert(Payment payment);
    void update(Payment payment);
    void delete(Payment payment); // 복합키를 위해 Payment 객체 사용
    
    // 결제번호 생성을 위한 시퀀스 조회
    @Select("SELECT nextval('payment_id_seq')")
    Long getNextPaymentSequence();
    
    // 클레임번호 생성을 위한 시퀀스 조회
    @Select("SELECT nextval('claim_id_seq')")
    Long getNextClaimSequence();
}