package com.vibepay.claim

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
@EnableFeignClients(basePackages = ["com.vibepay.claim.client"])
@EntityScan("com.api.app.entity", "com.vibepay.claim.entity", "com.vibepay.messaging")
@EnableJpaRepositories("com.vibepay.claim.repository", "com.vibepay.messaging")
@ComponentScan("com.vibepay.claim", "com.api.app.common.exception")
class ClaimApplication

fun main(args: Array<String>) {
    runApplication<ClaimApplication>(*args)
}
