package com.vibe.pay.domain.payment.factory;

import com.vibe.pay.domain.payment.adapter.PaymentGatewayAdapter;
import com.vibe.pay.enums.PgCompany;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * PG사별 PaymentGatewayAdapter 팩토리
 *
 * PG사에 따라 적절한 어댑터를 주입하여 반환합니다.
 * 팩토리 패턴을 통해 PG사별 로직을 캡슐화하고 확장성을 제공합니다.
 *
 * Technical Specification 참조:
 * - docs/v5/TechnicalSpecification/payment-and-pg-integration-spec.md
 *
 * @see PaymentGatewayAdapter
 * @see InicisAdapter
 * @see NicePayAdapter
 * @see TossAdapter
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentGatewayFactory {

    private final List<PaymentGatewayAdapter> adapters;

    /**
     * PG사에 해당하는 PaymentGatewayAdapter 반환
     *
     * @param pgCompany PG사 enum
     * @return 해당 PG사의 어댑터
     * @throws RuntimeException 지원하지 않는 PG사인 경우
     */
    public PaymentGatewayAdapter getAdapter(PgCompany pgCompany) {
        log.debug("Getting PaymentGatewayAdapter for PG company: {}", pgCompany);

        return adapters.stream()
                .filter(adapter -> adapter.supports(pgCompany))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Unsupported PG company: {}", pgCompany);
                    return new RuntimeException("Unsupported PG company: " + pgCompany);
                });
    }
}