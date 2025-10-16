package com.vibe.pay.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * PG사 설정 Properties
 *
 * application.yml의 pg 설정을 바인딩합니다.
 * Inicis, Nicepay 등 PG사별 API 연동 정보를 관리합니다.
 *
 * 사용 예시:
 * - pgConfigProperties.getInicis().getApiUrl()
 * - pgConfigProperties.getNicepay().getMid()
 */
@Component
@ConfigurationProperties(prefix = "pg")
@Getter
@Setter
public class PgConfigProperties {

    /**
     * Inicis PG 설정
     */
    private PgConfig inicis = new PgConfig();

    /**
     * Nicepay PG 설정
     */
    private PgConfig nicepay = new PgConfig();

    /**
     * PG사명으로 설정 조회
     *
     * @param pgCompanyName PG사명 (inicis, nicepay)
     * @return PG 설정
     */
    public PgConfig getConfig(String pgCompanyName) {
        Map<String, PgConfig> configMap = new HashMap<>();
        configMap.put("inicis", inicis);
        configMap.put("nicepay", nicepay);

        return configMap.getOrDefault(pgCompanyName.toLowerCase(), null);
    }

    /**
     * PG사별 공통 설정 클래스
     */
    @Getter
    @Setter
    public static class PgConfig {
        /**
         * PG사 활성화 여부
         */
        private boolean enabled = true;

        /**
         * 운영 API URL
         */
        private String apiUrl;

        /**
         * 테스트 API URL
         */
        private String testApiUrl;

        /**
         * 가맹점 ID (MID - Merchant ID)
         */
        private String mid;

        /**
         * API Key
         */
        private String apiKey;

        /**
         * Secret Key
         */
        private String secretKey;

        /**
         * 타임아웃 (ms)
         */
        private int timeout = 30000;

        /**
         * PG사 가중치 (라우팅에 사용)
         */
        private int weight = 50;

        /**
         * 현재 환경에 맞는 API URL 반환
         *
         * @param isTestMode 테스트 모드 여부
         * @return API URL
         */
        public String getEffectiveApiUrl(boolean isTestMode) {
            return isTestMode ? testApiUrl : apiUrl;
        }
    }
}
