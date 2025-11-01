package com.vibe.pay.backend.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * HTTP 통신 유틸리티
 *
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
@Slf4j
@Component
public class WebClientUtil {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public WebClientUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * POST 요청
     *
     * @param url  요청 URL
     * @param body 요청 본문
     * @return 응답 문자열
     */
    public String post(String url, Object body) {
        try {
            String jsonBody = objectMapper.writeValueAsString(body);
            log.debug("POST 요청: url={}, body={}", url, jsonBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.debug("POST 응답: status={}, body={}", response.statusCode(), response.body());

            return response.body();
        } catch (Exception e) {
            log.error("POST 요청 실패: url={}", url, e);
            throw new RuntimeException("HTTP POST request failed: " + url, e);
        }
    }

    /**
     * POST 요청 (헤더 포함)
     *
     * @param url     요청 URL
     * @param body    요청 본문
     * @param headers 추가 헤더
     * @return 응답 문자열
     */
    public String post(String url, Object body, java.util.Map<String, String> headers) {
        try {
            String jsonBody = objectMapper.writeValueAsString(body);
            log.debug("POST 요청: url={}, body={}, headers={}", url, jsonBody, headers);

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .timeout(Duration.ofSeconds(30));

            // 추가 헤더 설정
            if (headers != null) {
                headers.forEach(requestBuilder::header);
            }

            HttpRequest request = requestBuilder.build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.debug("POST 응답: status={}, body={}", response.statusCode(), response.body());

            return response.body();
        } catch (Exception e) {
            log.error("POST 요청 실패: url={}", url, e);
            throw new RuntimeException("HTTP POST request failed: " + url, e);
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
            log.debug("GET 요청: url={}", url);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.debug("GET 응답: status={}, body={}", response.statusCode(), response.body());

            return response.body();
        } catch (Exception e) {
            log.error("GET 요청 실패: url={}", url, e);
            throw new RuntimeException("HTTP GET request failed: " + url, e);
        }
    }
}
