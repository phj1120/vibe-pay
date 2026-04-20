package com.vibepay.goods

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@EnableJpaAuditing
@EntityScan("com.api.app.entity", "com.vibepay.goods.entity")
@EnableJpaRepositories("com.vibepay.goods.repository")
@ComponentScan("com.vibepay.goods", "com.api.app.common.exception")
class GoodsApplication

fun main(args: Array<String>) {
    runApplication<GoodsApplication>(*args)
}
