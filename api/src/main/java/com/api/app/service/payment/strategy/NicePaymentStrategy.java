package com.api.app.service.payment.strategy;

import com.api.app.common.exception.ApiError;
import com.api.app.common.exception.ApiException;
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
                throw new ApiException(ApiError.PAYMENT_APPROVAL_FAILED, "나이스 승인 응답이 없습니다");
            }

            log.info("Nice approval raw response: {}", responseBodyStr);

            // JSON 문자열을 DTO로 변환
            NiceApprovalResponse responseBody;
            try {
                responseBody = objectMapper.readValue(responseBodyStr, NiceApprovalResponse.class);
            } catch (Exception e) {
                log.error("Failed to parse Nice approval response: {}", responseBodyStr, e);
                throw new ApiException(ApiError.PAYMENT_APPROVAL_FAILED, 
                        "나이스 응답 파싱 실패: " + e.getMessage());
            }

            log.info("Nice approval response received. ResultCode={}", responseBody.getResultCode());

            // 응답 검증
            if (!"3001".equals(responseBody.getResultCode())) {  // 나이스는 3001이 신용카드 성공
                log.error("Nice approval failed. ResultCode={}, ResultMsg={}",
                        responseBody.getResultCode(), responseBody.getResultMsg());
                throw new ApiException(ApiError.PAYMENT_APPROVAL_FAILED, 
                        "나이스 결제 승인 실패: " + responseBody.getResultMsg());
            }

            // 승인 성공 응답 생성
            return com.api.app.dto.response.payment.PaymentApprovalResponse.builder()
                    .approveNo(responseBody.getAuthCode())      // 승인번호
                    .trdNo(responseBody.getTID())               // 거래ID
                    .amount(Long.parseLong(responseBody.getAmt()))  // 결제금액
                    .cardNo(responseBody.getCardNo())           // 카드번호
                    .cardCode(responseBody.getCardCode())       // 카드사 코드
                    .build();

        } catch (ApiException e) {
            // ApiException은 그대로 던짐
            throw e;
        } catch (Exception e) {
            log.error("Nice payment approval failed. orderNo={}", request.getOrderNo(), e);
            throw new ApiException(ApiError.PAYMENT_APPROVAL_FAILED, 
                    "나이스 결제 승인에 실패했습니다: " + e.getMessage());
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
    public void cancelPaymentByOrder(com.api.app.dto.request.payment.PaymentCancelRequest request) {
        log.info("Nice order cancel started. orderNo={}, tid={}, cancelAmount={}",
                request.getOrderNo(), request.getTransactionId(), request.getCancelAmount());

        try {
            // 취소 가능한 금액 조회
            Long cancelableAmount = request.getCancelableAmount();
            if (cancelableAmount == null) {
                throw new ApiException(ApiError.INVALID_PARAMETER, 
                        "취소 가능한 금액 정보가 필요합니다");
            }
            
            // 원 승인 금액 조회
            Long originalAmount = request.getOriginalAmount();
            if (originalAmount == null) {
                throw new ApiException(ApiError.INVALID_PARAMETER, 
                        "원 승인 금액 정보가 필요합니다");
            }
            
            // 취소 후 남은 금액 계산
            Long remainingAmount = cancelableAmount - request.getCancelAmount();
            
            // 부분 취소 판단: 원 승인 금액과 취소 가능한 금액이 다르면 이미 부분 취소된 상태
            // 또는 취소 후 남은 금액이 0보다 크면 부분 취소
            boolean isPartialCancel = !originalAmount.equals(cancelableAmount) || remainingAmount > 0;
            
            // 부분취소 코드 결정 (0: 전체취소, 1: 부분취소)
            String partialCancelCode = isPartialCancel ? "1" : "0";
            
            log.info("Nice cancel decision. originalAmount={}, cancelableAmount={}, cancelAmount={}, remainingAmount={}, isPartialCancel={}, partialCancelCode={}",
                    originalAmount, cancelableAmount, request.getCancelAmount(), remainingAmount, isPartialCancel, partialCancelCode);
            
            // 도메인 문서에 따라 나이스 취소 API 호출
            // POST https://pg-api.nicepay.co.kr/webapi/cancel_process.jsp
            String url = "https://pg-api.nicepay.co.kr/webapi/cancel_process.jsp";

            // 전문생성일시 생성
            String ediDate = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

            // SignData 생성: hex(sha256(MID + CancelAmt + EdiDate + MerchantKey))
            String signTarget = mid + request.getCancelAmount() + ediDate + merchantKey;
            String signData = sha256Hash(signTarget);

            // 요청 파라미터 생성 (form-urlencoded)
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("TID", request.getTransactionId());
            params.add("MID", mid);
            params.add("Moid", request.getOrderNo());
            params.add("CancelAmt", String.valueOf(request.getCancelAmount()));
            params.add("CancelMsg", request.getCancelReason());
            params.add("PartialCancelCode", partialCancelCode);
            params.add("EdiDate", ediDate);
            params.add("SignData", signData);
            params.add("CharSet", "utf-8");
            params.add("EdiType", "JSON");

            // HTTP 요청
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

            // String으로 받아서 수동 파싱 (나이스는 text/html로 JSON 응답)
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            String responseBodyStr = response.getBody();
            if (responseBodyStr == null || responseBodyStr.isEmpty()) {
                throw new ApiException(ApiError.PAYMENT_CANCEL_FAILED, "나이스 취소 응답이 없습니다");
            }

            log.info("Nice cancel raw response: {}", responseBodyStr);

            // JSON 문자열을 Map으로 변환
            Map<String, Object> responseBody;
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> parsedBody = objectMapper.readValue(responseBodyStr, Map.class);
                responseBody = parsedBody;
            } catch (Exception e) {
                log.error("Failed to parse Nice cancel response: {}", responseBodyStr, e);
                throw new ApiException(ApiError.PAYMENT_CANCEL_FAILED, 
                        "나이스 취소 응답 파싱 실패: " + e.getMessage());
            }

            String resultCode = (String) responseBody.get("ResultCode");
            String resultMsg = (String) responseBody.get("ResultMsg");

            // 나이스 취소 성공 코드: 2001
            if (!"2001".equals(resultCode)) {
                log.error("Nice cancel failed. orderNo={}, resultCode={}, resultMsg={}",
                        request.getOrderNo(), resultCode, resultMsg);
                throw new ApiException(ApiError.PAYMENT_CANCEL_FAILED, resultMsg);
            }

            log.info("Nice order cancel completed. orderNo={}, resultCode={}, resultMsg={}",
                    request.getOrderNo(), resultCode, resultMsg);

        } catch (ApiException e) {
            // ApiException은 그대로 던짐
            throw e;
        } catch (Exception e) {
            log.error("Nice order cancel failed. orderNo={}", request.getOrderNo(), e);
            throw new ApiException(ApiError.PAYMENT_CANCEL_FAILED, 
                    "나이스 주문 취소에 실패했습니다: " + e.getMessage());
        }
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
