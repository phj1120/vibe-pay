package com.vibe.pay.backend.paymentlog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/paymentlogs")
public class PaymentInterfaceRequestLogController {

    @Autowired
    private PaymentInterfaceRequestLogService paymentInterfaceRequestLogService;

    @PostMapping
    public PaymentInterfaceRequestLog createLog(@RequestBody PaymentInterfaceRequestLog log) {
        return paymentInterfaceRequestLogService.createLog(log);
    }

    @GetMapping
    public List<PaymentInterfaceRequestLog> getAllLogs() {
        return paymentInterfaceRequestLogService.getAllLogs();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentInterfaceRequestLog> getLogById(@PathVariable Long id) {
        return paymentInterfaceRequestLogService.getLogById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
