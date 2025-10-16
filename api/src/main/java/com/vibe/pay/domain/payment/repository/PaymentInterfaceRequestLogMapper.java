package com.vibe.pay.domain.payment.repository;

import com.vibe.pay.domain.payment.entity.PaymentInterfaceRequestLog;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Optional;

@Mapper
public interface PaymentInterfaceRequestLogMapper {
    void insert(PaymentInterfaceRequestLog log);
    Optional<PaymentInterfaceRequestLog> findByLogId(Long logId);
    List<PaymentInterfaceRequestLog> findByPaymentId(String paymentId);
    List<PaymentInterfaceRequestLog> findAll();
    void delete(Long logId);
}
