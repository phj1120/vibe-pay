package com.api.app.service.payment.method

import org.springframework.stereotype.Component

@Component
class PaymentMethodFactory(strategies: List<PaymentMethodStrategy>) {

    private val strategyMap: Map<String, PaymentMethodStrategy> =
        strategies.associateBy { it.getPayWayCode() }

    fun getStrategy(payWayCode: String): PaymentMethodStrategy =
        strategyMap[payWayCode]
            ?: throw IllegalArgumentException("지원하지 않는 결제 방식입니다: $payWayCode")
}
