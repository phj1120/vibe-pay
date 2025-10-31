package com.api.app.service.payment.strategy;

import com.api.app.dto.request.payment.InicisApprovalRequest;
import com.api.app.dto.request.payment.PaymentInitiateRequest;
import com.api.app.dto.response.payment.InicisApprovalResponse;
import com.api.app.dto.response.payment.PaymentInitiateResponse;
import com.api.app.emum.PAY005;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

/**
 * 이니시스 PG 전략 구현
 *
 * @author Claude
 * @version 1.0
 * @since 2025-10-31
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InicisPaymentStrategy implements PaymentGatewayStrategy {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${payment.inicis.mid}")
    private String mid;

    @Value("${payment.inicis.sign-key}")
    private String signKey;

    @Value("${payment.inicis.return-url}")
    private String returnUrl;

    @Value("${payment.inicis.close-url}")
    private String closeUrl;

    @Value("${payment.inicis.gopaymethod:Card}")
    private String gopaymethod;

    @Value("${payment.inicis.acceptmethod:below1000}")
    private String acceptmethod;

    @Override
    public PaymentInitiateResponse initiatePayment(PaymentInitiateRequest request) {
        log.info("Inicis payment initiate started. orderNumber={}", request.getOrderNumber());

        String timestamp = String.valueOf(System.currentTimeMillis());

        String mKey = sha256Hash(signKey);

        Map<String, String> formData = new HashMap<>();
        formData.put("mid", mid);
        formData.put("oid", request.getOrderNumber());
        formData.put("price", String.valueOf(request.getAmount()));
        formData.put("timestamp", timestamp);
        formData.put("mKey", mKey);
        formData.put("version", "1.0");
        formData.put("currency", "WON");
        formData.put("goodname", request.getProductName());
        formData.put("buyername", request.getBuyerName());
        formData.put("buyertel", request.getBuyerTel());
        formData.put("buyeremail", request.getBuyerEmail());
        formData.put("returnUrl", returnUrl);
        formData.put("closeUrl", closeUrl);
        formData.put("gopaymethod", gopaymethod);
        formData.put("acceptmethod", acceptmethod);
        formData.put("charset", "UTF-8");
        formData.put("use_chkfake", "");

        // signature: SHA256(oid + price + timestamp) - signKey 없음!
        String signatureData = String.format("oid=%s&price=%s&timestamp=%s",
                request.getOrderNumber(), request.getAmount(), timestamp);
        formData.put("signature", sha256Hash(signatureData));

        // verification: SHA256(oid + price + signKey + timestamp)
        String verificationData = String.format("oid=%s&price=%s&signKey=%s&timestamp=%s",
                request.getOrderNumber(), request.getAmount(), signKey, timestamp);
        formData.put("verification", sha256Hash(verificationData));

        log.info("Inicis payment initiate completed. orderNumber={}", request.getOrderNumber());

        return PaymentInitiateResponse.builder()
                .pgType("INICIS")
                .pgTypeCode("001") // PAY005.INICIS
                .paymentMethod(request.getPaymentMethod())
                .merchantId(mid)
                .merchantKey(signKey)
                .returnUrl(returnUrl)
                .formData(formData)
                .build();
    }

    @Override
    public com.api.app.dto.response.payment.PaymentApprovalResponse approvePayment(
            com.api.app.dto.request.payment.PaymentConfirmRequest request) {
        log.info("Inicis payment approval started. orderNo={}", request.getOrderNo());

        try {
            // 승인 요청 데이터 생성
            String timestamp = String.valueOf(System.currentTimeMillis());

            // signature: SHA256(authToken={authToken}&timestamp={timestamp})
            String signatureData = String.format("authToken=%s&timestamp=%s",
                    request.getAuthToken(), timestamp);
            String signature = sha256Hash(signatureData);

            // verification: SHA256(authToken={authToken}&signKey={signKey}&timestamp={timestamp})
            String verificationData = String.format("authToken=%s&signKey=%s&timestamp=%s",
                    request.getAuthToken(), signKey, timestamp);
            String verification = sha256Hash(verificationData);

            // 이니시스 전용 요청 DTO 생성
            InicisApprovalRequest inicisRequest = InicisApprovalRequest.builder()
                    .mid(mid)
                    .authToken(request.getAuthToken())
                    .timestamp(timestamp)
                    .signature(signature)
                    .verification(verification)
                    .charset("UTF-8")
                    .format("JSON")
                    .price(request.getPrice())
                    .build();

            // DTO를 MultiValueMap으로 변환
            MultiValueMap<String, String> params = convertToMultiValueMap(inicisRequest);

            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

            log.info("Inicis approval request prepared. authUrl={}, mid={}", request.getAuthUrl(), mid);

            // PG사 승인 API 호출 - 이니시스 전용 응답 DTO로 받기
            ResponseEntity<InicisApprovalResponse> response = restTemplate.postForEntity(
                    request.getAuthUrl(),
                    entity,
                    InicisApprovalResponse.class
            );

            InicisApprovalResponse responseBody = response.getBody();
            if (responseBody == null) {
                throw new RuntimeException("이니시스 승인 응답이 없습니다");
            }

            log.info("Inicis approval response received. resultCode={}", responseBody.getResultCode());

            // 응답 검증
            if (!"0000".equals(responseBody.getResultCode())) {
                log.error("Inicis approval failed. resultCode={}, resultMsg={}",
                        responseBody.getResultCode(), responseBody.getResultMsg());
                throw new RuntimeException("이니시스 결제 승인 실패: " + responseBody.getResultMsg());
            }

            // 승인 성공 응답 생성
            return com.api.app.dto.response.payment.PaymentApprovalResponse.builder()
                    .approveNo(responseBody.getApplNum())        // 승인번호
                    .trdNo(responseBody.getTid())                // 거래ID
                    .amount(Long.parseLong(responseBody.getTotPrice()))  // 결제금액
                    .cardNo(responseBody.getCARD_Num())          // 카드번호
                    .cardCode(responseBody.getCARD_Code())       // 카드사 코드
                    .build();

        } catch (Exception e) {
            log.error("Inicis payment approval failed. orderNo={}", request.getOrderNo(), e);
            throw new RuntimeException("이니시스 결제 승인에 실패했습니다: " + e.getMessage(), e);
        }
    }

    @Override
    public void cancelPayment(com.api.app.dto.request.payment.PaymentConfirmRequest request) {
        log.info("Inicis payment cancellation started. orderNo={}", request.getOrderNo());

        // TODO: 실제 PG사 망취소 API 호출 구현
        // POST {netCancelUrl}

        log.warn("Inicis payment cancellation not implemented yet. TODO: call PG netCancel API");
    }

    @Override
    public PAY005 getPgType() {
        return PAY005.INICIS;
    }

    /**
     * SHA-256 해싱
     */
    private String sha256Hash(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(text.getBytes(StandardCharsets.UTF_8));
            return String.format("%064x", new BigInteger(1, md.digest()));
        } catch (Exception e) {
            log.error("SHA-256 hashing failed", e);
            throw new RuntimeException("SHA-256 해싱에 실패했습니다", e);
        }
    }

    /**
     * DTO를 MultiValueMap으로 변환
     * ObjectMapper를 사용하여 자동 변환
     */
    private MultiValueMap<String, String> convertToMultiValueMap(Object dto) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        @SuppressWarnings("unchecked")
        Map<String, Object> map = objectMapper.convertValue(dto, Map.class);

        map.forEach((key, value) -> {
            if (value != null) {
                params.add(key, String.valueOf(value));
            }
        });

        return params;
    }
}
