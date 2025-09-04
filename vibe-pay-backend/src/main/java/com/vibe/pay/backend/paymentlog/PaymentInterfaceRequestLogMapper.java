package com.vibe.pay.backend.paymentlog;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface PaymentInterfaceRequestLogMapper {
    List<PaymentInterfaceRequestLog> findAll();
    PaymentInterfaceRequestLog findById(Long id);
    void insert(PaymentInterfaceRequestLog log);
    void update(PaymentInterfaceRequestLog log);
    void delete(Long id);
}