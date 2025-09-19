package com.vibe.pay.backend.paymentlog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentInterfaceRequestLogService {

    @Autowired
    private PaymentInterfaceRequestLogMapper paymentInterfaceRequestLogMapper;

    public PaymentInterfaceRequestLog createLog(PaymentInterfaceRequestLog log) {
        paymentInterfaceRequestLogMapper.insert(log);
        return log;
    }

    public Optional<PaymentInterfaceRequestLog> getLogById(Long logId) {
        return Optional.ofNullable(paymentInterfaceRequestLogMapper.findByLogId(logId));
    }

    public List<PaymentInterfaceRequestLog> getAllLogs() {
        return paymentInterfaceRequestLogMapper.findAll();
    }
}
