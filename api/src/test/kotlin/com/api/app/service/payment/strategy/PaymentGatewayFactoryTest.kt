package com.api.app.service.payment.strategy

import com.api.app.emum.PAY005
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock

class PaymentGatewayFactoryTest {

    @Test
    @DisplayName("selectByWeight returns the only registered strategy")
    fun selectByWeightReturnsOnlyRegisteredStrategy() {
        val strategy = mock(PaymentGatewayStrategy::class.java)
        doReturn(PAY005.INICIS).`when`(strategy).getPgType()
        val factory = PaymentGatewayFactory(listOf(strategy))

        repeat(10) {
            assertThat(factory.selectByWeight()).isSameAs(strategy)
        }
    }

    @Test
    @DisplayName("getStrategy returns matching strategy")
    fun getStrategyReturnsMatchingStrategy() {
        val inicis = mock(PaymentGatewayStrategy::class.java)
        val nice = mock(PaymentGatewayStrategy::class.java)
        doReturn(PAY005.INICIS).`when`(inicis).getPgType()
        doReturn(PAY005.NICE).`when`(nice).getPgType()
        val factory = PaymentGatewayFactory(listOf(inicis, nice))

        assertThat(factory.getStrategy(PAY005.NICE)).isSameAs(nice)
    }

    @Test
    @DisplayName("getStrategy throws for unsupported PG")
    fun getStrategyThrowsForUnsupportedPg() {
        val strategy = mock(PaymentGatewayStrategy::class.java)
        doReturn(PAY005.INICIS).`when`(strategy).getPgType()
        val factory = PaymentGatewayFactory(listOf(strategy))

        assertThatThrownBy { factory.getStrategy(PAY005.NICE) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("지원하지 않는 PG입니다: 나이스")
    }

    @Test
    @DisplayName("getByCode resolves strategy from PG code")
    fun getByCodeResolvesStrategyFromPgCode() {
        val strategy = mock(PaymentGatewayStrategy::class.java)
        doReturn(PAY005.NICE).`when`(strategy).getPgType()
        val factory = PaymentGatewayFactory(listOf(strategy))

        assertThat(factory.getByCode(PAY005.NICE.code)).isSameAs(strategy)
    }

    @Test
    @DisplayName("getByCode throws for unsupported PG code")
    fun getByCodeThrowsForUnsupportedPgCode() {
        val strategy = mock(PaymentGatewayStrategy::class.java)
        doReturn(PAY005.INICIS).`when`(strategy).getPgType()
        val factory = PaymentGatewayFactory(listOf(strategy))

        assertThatThrownBy { factory.getByCode("998") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("지원하지 않는 PG 코드입니다: 998")
    }
}
