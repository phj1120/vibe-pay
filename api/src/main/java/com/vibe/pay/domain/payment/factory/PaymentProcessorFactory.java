package com.vibe.pay.domain.payment.factory;

import com.vibe.pay.domain.payment.processor.PaymentProcessor;
import com.vibe.pay.enums.PaymentMethod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 결제 수단별 PaymentProcessor 팩토리
 *
 * 결제 수단에 따라 적절한 프로세서를 주입하여 반환합니다.
 * 팩토리 패턴을 통해 결제 수단별 로직을 캡슐화하고 확장성을 제공합니다.
 *
 * Technical Specification 참조:
 * - docs/v5/TechnicalSpecification/payment-and-pg-integration-spec.md
 *
 * @see PaymentProcessor
 * @see CreditCardPaymentProcessor
 * @see PointPaymentProcessor
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentProcessorFactory {

    private final List<PaymentProcessor> processors;

    /**
     * 결제 수단에 해당하는 PaymentProcessor 반환
     *
     * @param paymentMethod 결제 수단 enum
     * @return 해당 결제 수단의 프로세서
     * @throws RuntimeException 지원하지 않는 결제 수단인 경우
     */
    public PaymentProcessor getProcessor(PaymentMethod paymentMethod) {
        log.debug("Getting PaymentProcessor for payment method: {}", paymentMethod);

        return processors.stream()
                .filter(processor -> processor.supports(paymentMethod))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Unsupported payment method: {}", paymentMethod);
                    return new RuntimeException("Unsupported payment method: " + paymentMethod);
                });
    }
}