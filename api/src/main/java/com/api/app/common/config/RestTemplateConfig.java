package com.api.app.common.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * RestTemplate 설정
 *
 * @author Claude
 * @version 1.0
 * @since 2025-10-31
 */
@Configuration
public class RestTemplateConfig {

    /**
     * RestTemplate Bean 등록
     * PG사 API 호출에 사용
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);  // 연결 타임아웃 10초
        factory.setReadTimeout(30000);     // 읽기 타임아웃 30초

        return builder
                .requestFactory(() -> factory)
                .build();
    }
}
