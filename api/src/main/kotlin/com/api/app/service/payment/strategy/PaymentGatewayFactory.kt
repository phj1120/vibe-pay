package com.api.app.service.payment.strategy

import com.api.app.emum.PAY005
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class PaymentGatewayFactory(private val strategies: List<PaymentGatewayStrategy>) {

    private val log = LoggerFactory.getLogger(this::class.java)

    fun selectByWeight(): PaymentGatewayStrategy {
        val strategyMap = strategies.associateBy { it.getPgType() }

        val totalWeight = PAY005.entries.sumOf { it.referenceValue1.toInt() }
        val randomValue = Random.nextInt(totalWeight) + 1

        var currentWeight = 0
        for (pg in PAY005.entries) {
            currentWeight += pg.referenceValue1.toInt()
            if (randomValue <= currentWeight) {
                log.info("PG selected: {} (weight={})", pg.codeName, pg.referenceValue1)
                return strategyMap[pg] ?: strategies.first()
            }
        }

        log.warn("PG selection failed, using default strategy")
        return strategies.first()
    }

    fun getStrategy(pgType: PAY005): PaymentGatewayStrategy =
        strategies.firstOrNull { it.getPgType() == pgType }
            ?: throw IllegalArgumentException("지원하지 않는 PG입니다: ${pgType.codeName}")

    fun getByCode(pgTypeCode: String): PaymentGatewayStrategy {
        val pg = PAY005.findByCode(pgTypeCode)
            ?: throw IllegalArgumentException("지원하지 않는 PG 코드입니다: $pgTypeCode")
        return getStrategy(pg)
    }
}
