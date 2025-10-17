package com.vibe.pay.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * HTTP 통신 유틸리티
 *
 * WebClient 또는 RestTemplate을 사용한 HTTP 요청 처리를 담당합니다.
 * PG사 API 연동 시 사용됩니다.
 *
 * @author system
 * @version 1.0
 * @since 2025-10-17
 */
@Component
@Slf4j
public class WebClientUtil {

    private final RestTemplate restTemplate;

    public WebClientUtil() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * POST 요청 (application/x-www-form-urlencoded)
     *
     * @param url 요청 URL
     * @param params 요청 파라미터 (Map)
     * @return 응답 문자열
     */
    public String postFormUrlEncoded(String url, Map<String, String> params) {
        try {
            log.debug("POST request to {}: params={}", url, params);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            params.forEach(formData::add);

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            log.debug("POST response from {}: status={}, body={}",
                    url, response.getStatusCode(), response.getBody());

            return response.getBody();

        } catch (Exception e) {
            log.error("POST request failed to {}: {}", url, e.getMessage(), e);
            throw new RuntimeException("HTTP POST request failed: " + e.getMessage(), e);
        }
    }

    /**
     * POST 요청 (application/json)
     *
     * @param url 요청 URL
     * @param requestBody 요청 본문 (JSON 문자열 또는 객체)
     * @return 응답 문자열
     */
    public String postJson(String url, Object requestBody) {
        try {
            log.debug("POST JSON request to {}: body={}", url, requestBody);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Object> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            log.debug("POST JSON response from {}: status={}, body={}",
                    url, response.getStatusCode(), response.getBody());

            return response.getBody();

        } catch (Exception e) {
            log.error("POST JSON request failed to {}: {}", url, e.getMessage(), e);
            throw new RuntimeException("HTTP POST JSON request failed: " + e.getMessage(), e);
        }
    }

    /**
     * GET 요청
     *
     * @param url 요청 URL
     * @return 응답 문자열
     */
    public String get(String url) {
        try {
            log.debug("GET request to {}", url);

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            log.debug("GET response from {}: status={}, body={}",
                    url, response.getStatusCode(), response.getBody());

            return response.getBody();

        } catch (Exception e) {
            log.error("GET request failed to {}: {}", url, e.getMessage(), e);
            throw new RuntimeException("HTTP GET request failed: " + e.getMessage(), e);
        }
    }

    /**
     * POST 요청 (커스텀 헤더)
     *
     * @param url 요청 URL
     * @param requestBody 요청 본문
     * @param headers 커스텀 헤더
     * @return 응답 문자열
     */
    public String postWithHeaders(String url, Object requestBody, HttpHeaders headers) {
        try {
            log.debug("POST request with headers to {}: body={}, headers={}", url, requestBody, headers);

            HttpEntity<Object> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            log.debug("POST response from {}: status={}, body={}",
                    url, response.getStatusCode(), response.getBody());

            return response.getBody();

        } catch (Exception e) {
            log.error("POST request with headers failed to {}: {}", url, e.getMessage(), e);
            throw new RuntimeException("HTTP POST request failed: " + e.getMessage(), e);
        }
    }
}
