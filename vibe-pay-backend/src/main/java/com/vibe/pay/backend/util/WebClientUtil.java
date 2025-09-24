package com.vibe.pay.backend.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Component
public class WebClientUtil {

    private static final Logger log = LoggerFactory.getLogger(WebClientUtil.class);
    private final WebClient webClient;
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    public WebClientUtil() {
        this.webClient = WebClient.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
            .build();
    }

    /**
     * POST 요청 (JSON)
     */
    public <T, R> R postJson(String url, T requestDto, Class<R> responseClass) {
        try {
            log.info("POST JSON request to: {}", url);
            log.debug("Request body: {}", requestDto);

            R response = webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(requestDto))
                .retrieve()
                .bodyToMono(responseClass)
                .timeout(DEFAULT_TIMEOUT)
                .block();

            log.debug("Response: {}", response);
            return response;

        } catch (Exception e) {
            log.error("POST JSON request failed to {}: {}", url, e.getMessage(), e);
            throw new RuntimeException("API call failed: " + e.getMessage(), e);
        }
    }

    /**
     * POST 요청 (Form Data)
     */
    public <R> R postForm(String url, MultiValueMap<String, String> formData, Class<R> responseClass) {
        try {
            log.info("POST Form request to: {}", url);
            log.debug("Form data: {}", formData);

            R response = webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(responseClass)
                .timeout(DEFAULT_TIMEOUT)
                .block();

            log.debug("Response: {}", response);
            return response;

        } catch (Exception e) {
            log.error("POST Form request failed to {}: {}", url, e.getMessage(), e);
            throw new RuntimeException("API call failed: " + e.getMessage(), e);
        }
    }

    /**
     * POST 요청 (Custom Headers)
     */
    public <T, R> R postWithHeaders(String url, T requestDto, Map<String, String> headers, Class<R> responseClass) {
        try {
            log.info("POST request with headers to: {}", url);
            log.debug("Request body: {}, Headers: {}", requestDto, headers);

            // 헤더 추가
            WebClient.RequestBodySpec requestSpec = webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON);

            if (headers != null) {
                headers.forEach(requestSpec::header);
            }

            R response = requestSpec
                .body(BodyInserters.fromValue(requestDto))
                .retrieve()
                .bodyToMono(responseClass)
                .timeout(DEFAULT_TIMEOUT)
                .block();

            log.debug("Response: {}", response);
            return response;

        } catch (Exception e) {
            log.error("POST request with headers failed to {}: {}", url, e.getMessage(), e);
            throw new RuntimeException("API call failed: " + e.getMessage(), e);
        }
    }

    /**
     * GET 요청
     */
    public <R> R get(String url, Class<R> responseClass) {
        try {
            log.info("GET request to: {}", url);

            R response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(responseClass)
                .timeout(DEFAULT_TIMEOUT)
                .block();

            log.debug("Response: {}", response);
            return response;

        } catch (Exception e) {
            log.error("GET request failed to {}: {}", url, e.getMessage(), e);
            throw new RuntimeException("API call failed: " + e.getMessage(), e);
        }
    }

    /**
     * GET 요청 (Query Parameters)
     */
    public <R> R getWithParams(String url, Map<String, String> params, Class<R> responseClass) {
        try {
            log.info("GET request with params to: {}", url);
            log.debug("Params: {}", params);

            WebClient.RequestHeadersUriSpec<?> spec = webClient.get();

            // URL 빌딩
            WebClient.RequestHeadersSpec<?> uriSpec = spec.uri(uriBuilder -> {
                uriBuilder.path(url);
                if (params != null) {
                    params.forEach(uriBuilder::queryParam);
                }
                return uriBuilder.build();
            });

            R response = uriSpec
                .retrieve()
                .bodyToMono(responseClass)
                .timeout(DEFAULT_TIMEOUT)
                .block();

            log.debug("Response: {}", response);
            return response;

        } catch (Exception e) {
            log.error("GET request with params failed to {}: {}", url, e.getMessage(), e);
            throw new RuntimeException("API call failed: " + e.getMessage(), e);
        }
    }
}