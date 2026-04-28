package com.api.app.common.config

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestTemplate
import java.util.function.Supplier

@Configuration
class RestTemplateConfig {

    @Bean
    fun restTemplate(builder: RestTemplateBuilder): RestTemplate {
        val factory = SimpleClientHttpRequestFactory().apply {
            setConnectTimeout(10000)
            setReadTimeout(30000)
        }
        return builder.requestFactory(Supplier { factory }).build()
    }
}
