package com.api.app.service.payment.method;

import com.api.app.emum.PAY002;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 결제 방식 전략 팩토리
 *
 * @author Claude
 * @version 1.0
 * @since 2025-10-31
 */
@Component
@RequiredArgsConstructor
public class PaymentMethodFactory {

    private final List<PaymentMethodStrategy> strategies;
    private Map<String, PaymentMethodStrategy> strategyMap;

    /**
     * 결제 방식 코드로 전략 선택
     *
     * @param payWayCode 결제 방식 코드 (PAY002)
     * @return 결제 방식 전략
     */
    public PaymentMethodStrategy getStrategy(String payWayCode) {
        if (strategyMap == null) {
            initStrategyMap();
        }

        PaymentMethodStrategy strategy = strategyMap.get(payWayCode);
        if (strategy == null) {
            throw new IllegalArgumentException("지원하지 않는 결제 방식입니다: " + payWayCode);
        }

        return strategy;
    }

    /**
     * 우선순위 기반 전략 목록 반환
     * PAY002의 displaySequence 순서대로 반환
     *
     * @return 전략 목록 (우선순위 순)
     */
    public List<PaymentMethodStrategy> getStrategiesInPriorityOrder() {
        if (strategyMap == null) {
            initStrategyMap();
        }

        // PAY002의 displaySequence 순서대로 정렬
        return strategies.stream()
                .sorted((s1, s2) -> {
                    PAY002 pay1 = PAY002.findByCode(s1.getPayWayCode());
                    PAY002 pay2 = PAY002.findByCode(s2.getPayWayCode());
                    return Integer.compare(pay1.getDisplaySequence(), pay2.getDisplaySequence());
                })
                .toList();
    }

    private void initStrategyMap() {
        strategyMap = new HashMap<>();
        for (PaymentMethodStrategy strategy : strategies) {
            strategyMap.put(strategy.getPayWayCode(), strategy);
        }
    }
}
