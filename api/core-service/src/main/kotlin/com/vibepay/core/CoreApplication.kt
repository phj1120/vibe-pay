package com.vibepay.core

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
@EntityScan("com.api.app.entity", "com.vibepay.core.entity", "com.vibepay.messaging")
@EnableJpaRepositories("com.vibepay.core.repository", "com.vibepay.messaging")
@ComponentScan("com.vibepay.core", "com.api.app.common.exception")
class CoreApplication

fun main(args: Array<String>) {
    runApplication<CoreApplication>(*args)
}
