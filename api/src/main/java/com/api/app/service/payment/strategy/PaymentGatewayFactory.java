package com.api.app.service.payment.strategy;

import com.api.app.emum.PAY005;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * PG사 전략 Factory
 * PAY005 enum의 가중치(referenceValue1)를 기반으로 PG사 선택
 *
 * @author Claude
 * @version 1.0
 * @since 2025-10-31
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentGatewayFactory {

    private final List<PaymentGatewayStrategy> strategies;
    private final Random random = new Random();

    /**
     * 가중치 기반 PG사 선택
     *
     * @return 선택된 PG 전략
     */
    public PaymentGatewayStrategy selectByWeight() {
        // PG 전략 맵 생성 (PAY005 -> strategy)
        Map<PAY005, PaymentGatewayStrategy> strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                        PaymentGatewayStrategy::getPgType,
                        Function.identity()
                ));

        // 가중치 합계 계산
        int totalWeight = 0;
        for (PAY005 pg : PAY005.values()) {
            totalWeight += Integer.parseInt(pg.getReferenceValue1());
        }

        // 랜덤 선택
        int randomValue = random.nextInt(totalWeight) + 1;
        log.debug("PG selection - totalWeight={}, randomValue={}", totalWeight, randomValue);

        int currentWeight = 0;
        for (PAY005 pg : PAY005.values()) {
            currentWeight += Integer.parseInt(pg.getReferenceValue1());
            if (randomValue <= currentWeight) {
                log.info("PG selected: {} (weight={})", pg.getCodeName(), pg.getReferenceValue1());
                return strategyMap.get(pg);
            }
        }

        // 기본값: 첫 번째 전략
        log.warn("PG selection failed, using default strategy");
        return strategies.get(0);
    }

    /**
     * 코드로 PG 전략 조회
     *
     * @param pgTypeCode PG 타입 코드
     * @return PG 전략
     */
    public PaymentGatewayStrategy getByCode(String pgTypeCode) {
        PAY005 pg = PAY005.findByCode(pgTypeCode);
        return strategies.stream()
                .filter(s -> s.getPgType() == pg)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 PG입니다: " + pgTypeCode));
    }

    /**
     * PAY005 enum으로 전략 조회
     *
     * @param pgType PG enum
     * @return PG 전략
     */
    public PaymentGatewayStrategy getStrategy(String pgTypeCode) {
        return strategies.stream()
                .filter(s -> s.getPgType().isEquals(pgTypeCode))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 PG입니다: " + pgTypeCode));
    }
}
