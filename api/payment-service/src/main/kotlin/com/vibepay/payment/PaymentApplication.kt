package com.vibepay.payment

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.ComponentScan
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
@EnableFeignClients(basePackages = ["com.vibepay.payment.client"])
@EntityScan("com.api.app.entity", "com.vibepay.payment.entity", "com.vibepay.messaging")
@EnableJpaRepositories("com.vibepay.payment.repository", "com.vibepay.messaging")
@ComponentScan("com.vibepay.payment", "com.api.app.common.exception")
class PaymentApplication

fun main(args: Array<String>) {
    runApplication<PaymentApplication>(*args)
}
