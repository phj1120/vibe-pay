package com.api.app.service.payment.strategy;

import com.api.app.dto.request.payment.NiceApprovalRequest;
import com.api.app.dto.request.payment.PaymentInitiateRequest;
import com.api.app.dto.response.payment.NiceApprovalResponse;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 나이스 PG 전략 구현
 *
 * @author Claude
 * @version 1.0
 * @since 2025-10-31
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NicePaymentStrategy implements PaymentGatewayStrategy {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${payment.nice.mid}")
    private String mid;

    @Value("${payment.nice.merchant-key}")
    private String merchantKey;

    @Value("${payment.nice.return-url}")
    private String returnUrl;

    @Value("${payment.nice.cancel-url}")
    private String cancelUrl;

    @Override
    public PaymentInitiateResponse initiatePayment(PaymentInitiateRequest request) {
        log.info("Nice payment initiate started. orderNumber={}", request.getOrderNumber());

        String ediDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        Map<String, String> formData = new HashMap<>();
        formData.put("PayMethod", "CARD");
        formData.put("MID", mid);
        formData.put("Moid", request.getOrderNumber());
        formData.put("Amt", String.valueOf(request.getAmount()));
        formData.put("GoodsName", request.getProductName());
        formData.put("BuyerName", request.getBuyerName());
        formData.put("BuyerEmail", request.getBuyerEmail());
        formData.put("BuyerTel", request.getBuyerTel());
        formData.put("ReturnURL", returnUrl);
        formData.put("EdiDate", ediDate);
        formData.put("CharSet", "UTF-8");

        // SignData: EdiDate + MID + Amt + MerchantKey
        String signData = ediDate + mid + request.getAmount() + merchantKey;
        formData.put("SignData", sha256Hash(signData));

        log.info("Nice payment initiate completed. orderNumber={}", request.getOrderNumber());

        return PaymentInitiateResponse.builder()
                .pgType("NICE")
                .pgTypeCode("002") // PAY005.NICE
                .paymentMethod(request.getPaymentMethod())
                .merchantId(mid)
                .merchantKey(merchantKey)
                .returnUrl(returnUrl)
                .formData(formData)
                .build();
    }

    @Override
    public com.api.app.dto.response.payment.PaymentApprovalResponse approvePayment(
            com.api.app.dto.request.payment.PaymentConfirmRequest request) {
        log.info("Nice payment approval started. orderNo={}", request.getOrderNo());

        try {
            // 승인 요청 데이터 생성
            String ediDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

            // SignData: hex(sha256(AuthToken + MID + Amt + EdiDate + MerchantKey))
            String signData = request.getAuthToken() + request.getMid() + request.getAmount() + ediDate + merchantKey;
            String signature = sha256Hash(signData);

            // 나이스 전용 요청 DTO 생성
            NiceApprovalRequest niceRequest = NiceApprovalRequest.builder()
                    .tid(request.getTradeNo())       // 거래번호 (인증 응답 TxTid)
                    .authToken(request.getAuthToken())
                    .mid(request.getMid())
                    .amt(request.getAmount())
                    .ediDate(ediDate)
                    .signData(signature)
                    .charSet("UTF-8")
                    .ediType("JSON")
                    .build();

            // DTO를 MultiValueMap으로 변환 (@JsonProperty 사용하여 키 매핑)
            MultiValueMap<String, String> params = convertToMultiValueMap(niceRequest);

            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

            log.info("Nice approval request prepared. authUrl={}, TID={}, MID={}",
                    request.getAuthUrl(), request.getTradeNo(), request.getMid());

            // PG사 승인 API 호출 - String으로 받아서 수동 파싱 (나이스는 text/html로 JSON 응답)
            ResponseEntity<String> response = restTemplate.postForEntity(
                    request.getAuthUrl(),
                    entity,
                    String.class
            );

            String responseBodyStr = response.getBody();
            if (responseBodyStr == null || responseBodyStr.isEmpty()) {
                throw new RuntimeException("나이스 승인 응답이 없습니다");
            }

            log.info("Nice approval raw response: {}", responseBodyStr);

            // JSON 문자열을 DTO로 변환
            NiceApprovalResponse responseBody;
            try {
                responseBody = objectMapper.readValue(responseBodyStr, NiceApprovalResponse.class);
            } catch (Exception e) {
                log.error("Failed to parse Nice approval response: {}", responseBodyStr, e);
                throw new RuntimeException("나이스 응답 파싱 실패: " + e.getMessage(), e);
            }

            log.info("Nice approval response received. ResultCode={}", responseBody.getResultCode());

            // 응답 검증
            if (!"3001".equals(responseBody.getResultCode())) {  // 나이스는 3001이 신용카드 성공
                log.error("Nice approval failed. ResultCode={}, ResultMsg={}",
                        responseBody.getResultCode(), responseBody.getResultMsg());
                throw new RuntimeException("나이스 결제 승인 실패: " + responseBody.getResultMsg());
            }

            // 승인 성공 응답 생성
            return com.api.app.dto.response.payment.PaymentApprovalResponse.builder()
                    .approveNo(responseBody.getAuthCode())      // 승인번호
                    .trdNo(responseBody.getTID())               // 거래ID
                    .amount(Long.parseLong(responseBody.getAmt()))  // 결제금액
                    .cardNo(responseBody.getCardNo())           // 카드번호
                    .cardCode(responseBody.getCardCode())       // 카드사 코드
                    .build();

        } catch (Exception e) {
            log.error("Nice payment approval failed. orderNo={}", request.getOrderNo(), e);
            throw new RuntimeException("나이스 결제 승인에 실패했습니다: " + e.getMessage(), e);
        }
    }

    @Override
    public void cancelPayment(com.api.app.dto.request.payment.PaymentConfirmRequest request) {
        log.info("Nice payment cancellation started. orderNo={}", request.getOrderNo());

        // TODO: 실제 PG사 망취소 API 호출 구현
        // POST {netCancelUrl}

        log.warn("Nice payment cancellation not implemented yet. TODO: call PG netCancel API");
    }

    @Override
    public PAY005 getPgType() {
        return PAY005.NICE;
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
     * @JsonProperty 어노테이션을 참조하여 올바른 키로 매핑
     */
    private MultiValueMap<String, String> convertToMultiValueMap(Object dto) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        // ObjectMapper가 @JsonProperty를 참조하여 Map으로 변환
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
