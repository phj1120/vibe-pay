package com.vibe.pay.domain.payment.repository;

import com.vibe.pay.domain.payment.entity.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Optional;

@Mapper
public interface PaymentMapper {
    void insert(Payment payment);
    Optional<Payment> findByPaymentId(String paymentId);
    List<Payment> findByOrderId(String orderId);
    List<Payment> findByMemberId(Long memberId);
    List<Payment> findAll();
    void update(Payment payment);
    void delete(String paymentId);

    /**
     * 결제 시퀀스 번호 조회
     * payment_seq 시퀀스에서 다음 값을 가져옵니다.
     *
     * @return 다음 시퀀스 번호
     */
    Long getNextPaymentSequence();
}
