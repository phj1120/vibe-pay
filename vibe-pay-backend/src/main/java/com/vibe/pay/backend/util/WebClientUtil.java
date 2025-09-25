package com.vibe.pay.backend.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;
import org.springframework.util.LinkedMultiValueMap;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.nio.charset.Charset;

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

    /**
     * POST 요청 (Raw Form Data String으로 전송, Text 응답)
     * 나이스페이 API용 - Form Data String 버전
     */
    public String postFormDataForText(String url, String formData) {
        try {
            log.info("POST Form Data String request for text response to: {}", url);
            log.debug("Form data string: {}", formData);

            String response = webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .acceptCharset(Charset.forName("EUC-KR"))
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(DEFAULT_TIMEOUT)
                .block();

            log.debug("Text response: {}", response);
            return response;

        } catch (Exception e) {
            log.error("POST Form Data String request for text failed to {}: {}", url, e.getMessage(), e);
            throw new RuntimeException("API call failed: " + e.getMessage(), e);
        }
    }

    /**
     * POST 요청 (Text 응답, Form Data, EUC-KR 인코딩)
     * 나이스페이 API용 - Map 버전
     */
    public String postForText(String url, Map<String, Object> params) {
        try {
            log.info("POST Form request for text response to: {}", url);
            log.debug("Form params: {}", params);

            // Map을 MultiValueMap으로 변환
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            if (params != null) {
                params.forEach((key, value) -> {
                    if (value != null) {
                        formData.add(key, value.toString());
                    }
                });
            }

            String response = webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .acceptCharset(Charset.forName("EUC-KR"))
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(String.class)
                .timeout(DEFAULT_TIMEOUT)
                .block();

            log.debug("Text response: {}", response);
            return response;

        } catch (Exception e) {
            log.error("POST Form request for text failed to {}: {}", url, e.getMessage(), e);
            throw new RuntimeException("API call failed: " + e.getMessage(), e);
        }
    }

    /**
     * POST 요청 (Text 응답, DTO를 Form Data로 변환, EUC-KR 인코딩)
     * 나이스페이 API용 - DTO 버전
     */
    public <T> String postDtoForText(String url, T dto) {
        try {
            log.info("POST DTO Form request for text response to: {}", url);
            log.debug("DTO: {}", dto);

            // DTO를 MultiValueMap으로 변환 (리플렉션 사용)
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

            if (dto != null) {
                java.lang.reflect.Field[] fields = dto.getClass().getDeclaredFields();
                for (java.lang.reflect.Field field : fields) {
                    field.setAccessible(true);
                    try {
                        Object value = field.get(dto);
                        if (value != null) {
                            formData.add(field.getName(), value.toString());
                        }
                    } catch (IllegalAccessException e) {
                        log.warn("Failed to access field {}: {}", field.getName(), e.getMessage());
                    }
                }
            }

            String response = webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .acceptCharset(Charset.forName("EUC-KR"))
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(String.class)
                .timeout(DEFAULT_TIMEOUT)
                .block();

            log.debug("Text response: {}", response);
            return response;

        } catch (Exception e) {
            log.error("POST DTO Form request for text failed to {}: {}", url, e.getMessage(), e);
            throw new RuntimeException("API call failed: " + e.getMessage(), e);
        }
    }

    /**
     * POST 요청 (DTO 응답, Form Data 전송, EUC-KR 인코딩)
     * 나이스페이 API용 - DTO를 전송하고 text 응답을 DTO로 파싱
     */
    public <T, R> R postDtoForDto(String url, T requestDto, Class<R> responseClass) {
        try {
            log.info("POST DTO Form request for DTO response to: {}", url);
            log.debug("Request DTO: {}", requestDto);

            // 먼저 text로 응답 받기
            String textResponse = postDtoForText(url, requestDto);

            // text를 DTO로 파싱 (JSON 형식 우선 확인)
            R responseDto;
            if (textResponse.trim().startsWith("{") && textResponse.trim().endsWith("}")) {
                // JSON 응답인 경우
                responseDto = parseJsonToDto(textResponse, responseClass);
            } else {
                // key=value 응답인 경우
                responseDto = parseTextToDto(textResponse, responseClass);
            }

            log.debug("Parsed response DTO: {}", responseDto);
            return responseDto;

        } catch (Exception e) {
            log.error("POST DTO Form request for DTO failed to {}: {}", url, e.getMessage(), e);
            throw new RuntimeException("API call failed: " + e.getMessage(), e);
        }
    }

    private <R> R parseJsonToDto(String textResponse, Class<R> responseClass) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.UPPER_CAMEL_CASE);
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            return objectMapper.readValue(textResponse, responseClass);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * key=value&key=value 형식의 text를 DTO로 파싱
     */
    private <R> R parseTextToDto(String text, Class<R> responseClass) {
        try {
            R instance = responseClass.getDeclaredConstructor().newInstance();

            if (text != null && !text.trim().isEmpty()) {
                String[] pairs = text.split("&");

                for (String pair : pairs) {
                    String[] keyValue = pair.split("=", 2);
                    if (keyValue.length == 2) {
                        String key = keyValue[0];
                        String value = java.net.URLDecoder.decode(keyValue[1], "UTF-8");

                        try {
                            java.lang.reflect.Field field = responseClass.getDeclaredField(key);
                            field.setAccessible(true);
                            field.set(instance, value);
                        } catch (NoSuchFieldException e) {
                            log.debug("Field {} not found in {}, skipping", key, responseClass.getSimpleName());
                        }
                    }
                }
            }

            return instance;

        } catch (Exception e) {
            log.error("Failed to parse text to DTO: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse response: " + e.getMessage(), e);
        }
    }
}