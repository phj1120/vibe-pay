package com.api.app.service.payment;

import com.api.app.dto.request.payment.PaymentInitiateRequest;
import com.api.app.dto.response.payment.PaymentInitiateResponse;
import com.api.app.service.payment.strategy.PaymentGatewayFactory;
import com.api.app.service.payment.strategy.PaymentGatewayStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 결제 서비스 구현
 *
 * @author Claude
 * @version 1.0
 * @since 2025-10-31
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentGatewayFactory paymentGatewayFactory;

    @Override
    public PaymentInitiateResponse initiatePayment(PaymentInitiateRequest request) {
        log.info("Payment initiate started. orderNumber={}, amount={}",
                request.getOrderNumber(), request.getAmount());

        // 가중치 기반 PG 선택
        PaymentGatewayStrategy strategy = paymentGatewayFactory.selectByWeight();

        // PG사별 결제 초기화
        PaymentInitiateResponse response = strategy.initiatePayment(request);

        log.info("Payment initiate completed. orderNumber={}, pgType={}",
                request.getOrderNumber(), response.getPgType());

        return response;
    }
}
