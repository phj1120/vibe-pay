package com.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Vibe Pay API Application
 *
 * Spring Boot 메인 애플리케이션 클래스
 * com.vibe.pay 패키지의 모든 컴포넌트를 스캔합니다.
 */
@SpringBootApplication(scanBasePackages = {"com.vibe.pay", "com.api"})
public class ApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }

}
