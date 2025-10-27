package com.vibe.pay.backend.paymentlog;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
@Mapper
public interface PaymentInterfaceRequestLogMapper {
    void insert(PaymentInterfaceRequestLog log);

    PaymentInterfaceRequestLog selectById(Long logId);

    List<PaymentInterfaceRequestLog> selectByPaymentId(String paymentId);

    List<PaymentInterfaceRequestLog> selectAll();
}
