package com.vibe.pay.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * CORS 설정 Properties
 *
 * application.yml의 cors 설정을 바인딩합니다.
 * @ConfigurationProperties를 사용하여 복잡한 설정 구조를 안전하게 바인딩할 수 있습니다.
 */
@Component
@ConfigurationProperties(prefix = "cors")
public class CorsConfigProperties {

    /**
     * 허용할 Origin 목록
     * 예: http://localhost:3000, https://example.com
     */
    private List<String> allowedOrigins;

    /**
     * 허용할 HTTP 메소드 목록
     * 예: GET, POST, PUT, DELETE
     */
    private List<String> allowedMethods;

    /**
     * 허용할 헤더 목록
     * 예: Content-Type, Authorization
     */
    private List<String> allowedHeaders;

    /**
     * 노출할 헤더 목록
     * 클라이언트에서 접근 가능한 응답 헤더
     */
    private List<String> exposedHeaders;

    /**
     * 인증 정보 포함 여부
     * 쿠키, Authorization 헤더 등을 포함할지 설정
     */
    private boolean allowCredentials;

    /**
     * Preflight 요청 캐시 시간 (초)
     * OPTIONS 요청의 결과를 캐시하는 시간
     */
    private long maxAge;

    // Getters and Setters
    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public List<String> getAllowedMethods() {
        return allowedMethods;
    }

    public void setAllowedMethods(List<String> allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    public List<String> getAllowedHeaders() {
        return allowedHeaders;
    }

    public void setAllowedHeaders(List<String> allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
    }

    public List<String> getExposedHeaders() {
        return exposedHeaders;
    }

    public void setExposedHeaders(List<String> exposedHeaders) {
        this.exposedHeaders = exposedHeaders;
    }

    public boolean isAllowCredentials() {
        return allowCredentials;
    }

    public void setAllowCredentials(boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }

    public long getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(long maxAge) {
        this.maxAge = maxAge;
    }
}