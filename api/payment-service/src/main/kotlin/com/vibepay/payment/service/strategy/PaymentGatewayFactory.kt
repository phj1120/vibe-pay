package com.vibepay.payment.service.strategy

import com.api.app.emum.PAY005
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class PaymentGatewayFactory(private val strategies: List<PaymentGatewayStrategy>) {

    fun selectByWeight(): PaymentGatewayStrategy {
        val map = strategies.associateBy { it.getPgType() }
        val total = PAY005.entries.sumOf { it.referenceValue1.toInt() }
        var current = 0
        val rand = Random.nextInt(total) + 1
        for (pg in PAY005.entries) {
            current += pg.referenceValue1.toInt()
            if (rand <= current) return map[pg] ?: strategies.first()
        }
        return strategies.first()
    }

    fun getStrategy(pgType: PAY005): PaymentGatewayStrategy =
        strategies.firstOrNull { it.getPgType() == pgType }
            ?: throw IllegalArgumentException("지원하지 않는 PG: ${pgType.codeName}")

    fun getByCode(pgTypeCode: String): PaymentGatewayStrategy {
        val pg = PAY005.findByCode(pgTypeCode) ?: throw IllegalArgumentException("지원하지 않는 PG 코드: $pgTypeCode")
        return getStrategy(pg)
    }
}
