package com.api.app.service.payment.method

import com.api.app.emum.PAY002
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock

class PaymentMethodFactoryTest {

    @Test
    @DisplayName("getStrategy returns registered strategy")
    fun getStrategyReturnsRegisteredStrategy() {
        val cardStrategy = mock(PaymentMethodStrategy::class.java)
        val pointStrategy = mock(PaymentMethodStrategy::class.java)
        given(cardStrategy.getPayWayCode()).willReturn(PAY002.CREDIT_CARD.code)
        given(pointStrategy.getPayWayCode()).willReturn(PAY002.POINT.code)

        val factory = PaymentMethodFactory(listOf(cardStrategy, pointStrategy))

        assertThat(factory.getStrategy(PAY002.POINT.code)).isSameAs(pointStrategy)
    }

    @Test
    @DisplayName("getStrategy throws for unsupported pay way")
    fun getStrategyThrowsForUnsupportedPayWay() {
        val cardStrategy = mock(PaymentMethodStrategy::class.java)
        given(cardStrategy.getPayWayCode()).willReturn(PAY002.CREDIT_CARD.code)

        val factory = PaymentMethodFactory(listOf(cardStrategy))

        assertThatThrownBy { factory.getStrategy("999") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("지원하지 않는 결제 방식입니다: 999")
    }
}
