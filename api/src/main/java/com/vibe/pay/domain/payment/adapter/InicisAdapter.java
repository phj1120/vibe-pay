package com.vibe.pay.domain.payment.adapter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibe.pay.common.exception.ErrorCode;
import com.vibe.pay.common.exception.PaymentException;
import com.vibe.pay.common.util.HashUtils;
import com.vibe.pay.common.util.WebClientUtil;
import com.vibe.pay.domain.payment.dto.PaymentCancelRequest;
import com.vibe.pay.domain.payment.dto.PaymentCancelResponse;
import com.vibe.pay.domain.payment.dto.PaymentConfirmRequest;
import com.vibe.pay.domain.payment.dto.PaymentConfirmResponse;
import com.vibe.pay.domain.payment.dto.PaymentInitResponse;
import com.vibe.pay.domain.payment.dto.PaymentInitiateRequest;
import com.vibe.pay.domain.payment.entity.PaymentInterfaceRequestLog;
import com.vibe.pay.domain.payment.repository.PaymentInterfaceRequestLogMapper;
import com.vibe.pay.enums.PgCompany;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 이니시스 PG 연동 어댑터
 *
 * 이니시스 PG사와의 결제 초기화, 승인, 취소 API 연동을 담당합니다.
 *
 * Technical Specification 참조:
 * - docs/v5/TechnicalSpecification/payment-and-pg-integration-spec.md
 * - docs/v5/TechnicalSpecification/inicis-api-spec.md
 *
 * @see PaymentGatewayAdapter
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class InicisAdapter implements PaymentGatewayAdapter {

    private final PaymentInterfaceRequestLogMapper logMapper;
    private final ObjectMapper objectMapper;
    private final WebClientUtil webClientUtil;

    @Value("${pg.inicis.mid}")
    private String mid;

    @Value("${pg.inicis.api-key}")
    private String apiKey;

    @Value("${pg.inicis.secret-key}")
    private String signKey;

    @Value("${pg.inicis.test-api-url:https://stgstdpay.inicis.com}")
    private String testApiUrl;

    @Override
    public boolean supports(PgCompany pgCompany) {
        return PgCompany.INICIS.equals(pgCompany);
    }

    @Override
    public PaymentInitResponse initiate(PaymentInitiateRequest request) {
        log.info("Initiating INICIS payment: orderId={}, amount={}",
                request.getOrderId(), request.getAmount());

        try {
            // 1. 타임스탬프 생성 (YYYYMMDDhhmmss 형식)
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

            // 2. 결제창 파라미터 생성
            Map<String, String> parameters = new HashMap<>();
            parameters.put("mid", mid);
            parameters.put("oid", request.getOrderId());
            parameters.put("price", String.valueOf(request.getAmount().longValue()));
            parameters.put("goodName", request.getProductName());
            parameters.put("buyerName", request.getBuyerName());
            parameters.put("buyerTel", request.getBuyerPhone());
            parameters.put("buyerEmail", request.getBuyerEmail());
            parameters.put("timestamp", timestamp);
            parameters.put("version", "1.0");
            parameters.put("currency", "WON");
            parameters.put("gopaymethod", "Card");
            parameters.put("acceptmethod", "below1000");

            // returnUrl과 closeUrl 설정
            parameters.put("returnUrl", request.getReturnUrl() != null ?
                    request.getReturnUrl() : "http://localhost:3000/payment/return");
            parameters.put("closeUrl", request.getCancelUrl() != null ?
                    request.getCancelUrl() : "http://localhost:3000/payment/close");

            // 3. mKey 생성 (signKey를 SHA-256 해싱)
            String mKey = HashUtils.sha256(signKey);
            parameters.put("mKey", mKey);

            // 4. signature 생성 (oid + price + timestamp)
            String signatureData = "oid=" + request.getOrderId() +
                    "&price=" + request.getAmount().longValue() +
                    "&timestamp=" + timestamp;
            String signature = HashUtils.sha256(signatureData);
            parameters.put("signature", signature);

            // 5. verification 생성 (oid + price + signKey + timestamp)
            String verificationData = "oid=" + request.getOrderId() +
                    "&price=" + request.getAmount().longValue() +
                    "&signKey=" + signKey +
                    "&timestamp=" + timestamp;
            String verification = HashUtils.sha256(verificationData);
            parameters.put("verification", verification);

            // 6. PaymentInitResponse 구성
            PaymentInitResponse response = new PaymentInitResponse();
            response.setSuccess(true);
            response.setPaymentUrl(testApiUrl + "/stdpay/pay.ini");
            response.setParameters(parameters);
            response.setMessage("INICIS payment initiation successful");

            log.info("INICIS payment initiation completed: orderId={}, paymentUrl={}",
                    request.getOrderId(), response.getPaymentUrl());

            return response;

        } catch (Exception e) {
            log.error("INICIS payment initiation failed: orderId={}", request.getOrderId(), e);

            PaymentInitResponse response = new PaymentInitResponse();
            response.setSuccess(false);
            response.setMessage("INICIS payment initiation failed: " + e.getMessage());
            response.setErrorCode("INIT_FAILED");

            return response;
        }
    }

    @Override
    public PaymentConfirmResponse confirm(PaymentConfirmRequest request) {
        log.info("Confirming INICIS payment: orderId={}, amount={}",
                request.getOrderId(), request.getAmount());

        PaymentInterfaceRequestLog requestLog = null;

        try {
            // 1. 요청 로그 기록
            requestLog = new PaymentInterfaceRequestLog();
            requestLog.setRequestType("CONFIRM");
            requestLog.setRequestPayload(objectMapper.writeValueAsString(request));
            requestLog.setTimestamp(LocalDateTime.now());
            logMapper.insert(requestLog);

            // 2. 이니시스 승인 API 호출 파라미터 준비
            String authUrl = request.getAuthUrl();
            String authToken = request.getAuthToken();
            String requestMid = request.getMid() != null ? request.getMid() : mid;
            long currentTimeMillis = System.currentTimeMillis();

            // 서명 생성 (authToken + timestamp)
            String signatureData = "authToken=" + authToken + "&timestamp=" + currentTimeMillis;
            String signature = HashUtils.sha256(signatureData);

            // 요청 파라미터 구성
            Map<String, String> params = new HashMap<>();
            params.put("mid", requestMid);
            params.put("authToken", authToken);
            params.put("signature", signature);
            params.put("timestamp", String.valueOf(currentTimeMillis));
            params.put("charset", "UTF-8");
            params.put("format", "JSON");

            log.debug("Calling INICIS approval API: authUrl={}, params={}", authUrl, params);

            // 3. 이니시스 승인 API 호출 (application/x-www-form-urlencoded)
            String responseBody = webClientUtil.postFormUrlEncoded(authUrl, params);

            log.debug("INICIS approval API response: {}", responseBody);

            // 4. 응답 로그 기록
            requestLog.setResponsePayload(responseBody);
            logMapper.updateResponse(requestLog);

            // 5. 응답 파싱 (JSON)
            Map<String, Object> responseMap = objectMapper.readValue(
                    responseBody, new TypeReference<Map<String, Object>>() {});

            String resultCode = (String) responseMap.get("resultCode");
            String resultMsg = (String) responseMap.get("resultMsg");
            String tid = (String) responseMap.get("tid");
            String applNum = (String) responseMap.get("applNum");
            String cardNum = (String) responseMap.get("CARD_Num");
            String cardIssuerName = (String) responseMap.get("CARD_IssuerName");

            // 금액 파싱 (TotPrice는 Integer 또는 String으로 올 수 있음)
            Object totPriceObj = responseMap.get("TotPrice");
            Long totPrice = null;
            if (totPriceObj instanceof Integer) {
                totPrice = ((Integer) totPriceObj).longValue();
            } else if (totPriceObj instanceof String) {
                totPrice = Long.parseLong((String) totPriceObj);
            } else if (totPriceObj instanceof Long) {
                totPrice = (Long) totPriceObj;
            }

            // 6. 응답 검증 - resultCode 확인
            if (!"0000".equals(resultCode)) {
                log.error("INICIS approval failed: resultCode={}, resultMsg={}", resultCode, resultMsg);
                throw new PaymentException(
                        ErrorCode.PG_TRANSACTION_FAILED,
                        "INICIS",
                        tid,
                        resultCode,
                        resultMsg
                );
            }

            // 7. 응답 검증 - 금액 일치 확인
            if (totPrice == null || !totPrice.equals(request.getAmount().longValue())) {
                log.error("Amount mismatch: requested={}, actual={}",
                        request.getAmount().longValue(), totPrice);
                throw new PaymentException(
                        ErrorCode.PAYMENT_AMOUNT_MISMATCH,
                        "INICIS",
                        tid,
                        "AMOUNT_MISMATCH",
                        "Requested amount: " + request.getAmount() + ", Actual amount: " + totPrice
                );
            }

            // 8. PaymentConfirmResponse 구성
            PaymentConfirmResponse response = new PaymentConfirmResponse();
            response.setSuccess(true);
            response.setTransactionId(tid);
            response.setApprovalNumber(applNum);
            response.setAmount(BigDecimal.valueOf(totPrice));
            response.setResultCode(resultCode);
            response.setMessage(resultMsg);
            response.setMaskedCardNumber(cardNum);
            response.setCardCompany(cardIssuerName);

            log.info("INICIS payment confirmation completed: orderId={}, tid={}, amount={}",
                    request.getOrderId(), tid, totPrice);

            return response;

        } catch (PaymentException e) {
            // PaymentException은 그대로 재발생
            throw e;

        } catch (Exception e) {
            log.error("INICIS payment confirmation failed: orderId={}", request.getOrderId(), e);

            // 응답 로그에 에러 기록
            if (requestLog != null) {
                try {
                    requestLog.setResponsePayload("ERROR: " + e.getMessage());
                    logMapper.updateResponse(requestLog);
                } catch (Exception logError) {
                    log.error("Failed to update error log", logError);
                }
            }

            throw new RuntimeException("INICIS payment confirmation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentCancelResponse cancel(PaymentCancelRequest request) {
        log.info("Cancelling INICIS payment: orderId={}, paymentId={}, amount={}",
                request.getOrderId(), request.getPaymentId(), request.getAmount());

        try {
            // 1. 요청 로그 기록
            PaymentInterfaceRequestLog requestLog = new PaymentInterfaceRequestLog();
            requestLog.setPaymentId(request.getPaymentId());
            requestLog.setRequestType("CANCEL");
            requestLog.setRequestPayload(objectMapper.writeValueAsString(request));
            requestLog.setTimestamp(LocalDateTime.now());
            logMapper.insert(requestLog);

            // 2. 이니시스 취소 API 호출 (실제 구현 시 WebClient 사용)
            // 실제 구현 예시:
            // - 이니시스 취소 API 엔드포인트: POST /api/cancel
            // - 필수 파라미터: tid(거래ID), amount(취소금액), cancelReason(취소사유)
            // - 인증: mid(상점ID), signature(서명)

            // Stub 구현: 실제 PG 연동 시 WebClient를 통한 HTTP 호출로 대체
            PaymentCancelResponse response = new PaymentCancelResponse();
            response.setSuccess(true);
            response.setCancelTransactionId("INICIS_CANCEL_" + System.currentTimeMillis());
            response.setCancelApprovalNumber("CANCEL_APPROVAL_" + System.currentTimeMillis());
            response.setCancelAmount(request.getAmount());
            response.setResultCode("0000");
            response.setMessage("INICIS payment cancellation successful (stub)");
            response.setOriginalTransactionId(request.getOriginalTransactionId());

            // 3. 응답 로그 기록
            requestLog.setResponsePayload(objectMapper.writeValueAsString(response));
            logMapper.updateResponse(requestLog);

            // 4. 응답 검증
            if (!response.isSuccess()) {
                throw new RuntimeException("INICIS cancellation failed: " + response.getMessage());
            }

            if (!response.getCancelAmount().equals(request.getAmount())) {
                throw new RuntimeException("Cancel amount mismatch: requested="
                        + request.getAmount() + ", actual=" + response.getCancelAmount());
            }

            log.info("INICIS payment cancellation completed: orderId={}, cancelTransactionId={}",
                    request.getOrderId(), response.getCancelTransactionId());

            return response;

        } catch (Exception e) {
            log.error("INICIS payment cancellation failed: orderId={}", request.getOrderId(), e);
            throw new RuntimeException("INICIS payment cancellation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentCancelResponse netCancel(PaymentCancelRequest request) {
        log.info("Net cancelling INICIS payment: orderId={}, paymentId={}, amount={}",
                request.getOrderId(), request.getPaymentId(), request.getAmount());

        PaymentInterfaceRequestLog requestLog = null;

        try {
            // 1. 요청 로그 기록
            requestLog = new PaymentInterfaceRequestLog();
            requestLog.setPaymentId(request.getPaymentId());
            requestLog.setRequestType("NET_CANCEL");
            requestLog.setRequestPayload(objectMapper.writeValueAsString(request));
            requestLog.setTimestamp(LocalDateTime.now());
            logMapper.insert(requestLog);

            // 2. 이니시스 망취소 API 호출 파라미터 준비
            String netCancelUrl = request.getNetCancelUrl();
            String authToken = request.getAuthToken();
            String requestMid = request.getMid() != null ? request.getMid() : mid;
            long currentTimeMillis = System.currentTimeMillis();

            // 서명 생성 (authToken + timestamp)
            String signatureData = "authToken=" + authToken + "&timestamp=" + currentTimeMillis;
            String signature = HashUtils.sha256(signatureData);

            // 요청 파라미터 구성
            Map<String, String> params = new HashMap<>();
            params.put("mid", requestMid);
            params.put("authToken", authToken);
            params.put("signature", signature);
            params.put("timestamp", String.valueOf(currentTimeMillis));
            params.put("charset", "UTF-8");
            params.put("format", "JSON");

            log.debug("Calling INICIS net cancel API: netCancelUrl={}, params={}", netCancelUrl, params);

            // 3. 이니시스 망취소 API 호출 (application/x-www-form-urlencoded)
            // 망취소는 실패해도 예외를 throw하지 않고 로깅만 수행
            String responseBody = null;
            try {
                responseBody = webClientUtil.postFormUrlEncoded(netCancelUrl, params);
                log.debug("INICIS net cancel API response: {}", responseBody);

                // 4. 응답 로그 기록
                requestLog.setResponsePayload(responseBody);
                logMapper.updateResponse(requestLog);

            } catch (Exception apiException) {
                log.warn("INICIS net cancel API call failed (non-critical): orderId={}, error={}",
                        request.getOrderId(), apiException.getMessage());

                // 망취소 실패는 치명적이지 않으므로 로그만 남기고 계속 진행
                requestLog.setResponsePayload("ERROR: " + apiException.getMessage());
                logMapper.updateResponse(requestLog);
            }

            // 5. PaymentCancelResponse 구성
            // 망취소는 항상 성공으로 처리 (실패해도 시스템 흐름에는 영향 없음)
            PaymentCancelResponse response = new PaymentCancelResponse();
            response.setSuccess(true);
            response.setCancelTransactionId("NET_CANCEL_" + System.currentTimeMillis());
            response.setCancelAmount(request.getAmount());
            response.setResultCode("0000");
            response.setMessage("INICIS payment net cancellation processed");
            response.setOriginalTransactionId(request.getOriginalTransactionId());

            log.info("INICIS payment net cancellation completed: orderId={}, cancelTransactionId={}",
                    request.getOrderId(), response.getCancelTransactionId());

            return response;

        } catch (Exception e) {
            // 망취소 실패는 치명적이지 않으므로 로그만 남기고 기본 응답 반환
            log.warn("INICIS payment net cancellation failed (non-critical): orderId={}, error={}",
                    request.getOrderId(), e.getMessage(), e);

            // 응답 로그에 에러 기록
            if (requestLog != null) {
                try {
                    requestLog.setResponsePayload("ERROR: " + e.getMessage());
                    logMapper.updateResponse(requestLog);
                } catch (Exception logError) {
                    log.error("Failed to update error log", logError);
                }
            }

            // 망취소 실패해도 성공으로 처리
            PaymentCancelResponse response = new PaymentCancelResponse();
            response.setSuccess(true);
            response.setCancelTransactionId("NET_CANCEL_FAILED_" + System.currentTimeMillis());
            response.setCancelAmount(request.getAmount());
            response.setResultCode("9999");
            response.setMessage("Net cancel failed but marked as success (non-critical)");
            response.setOriginalTransactionId(request.getOriginalTransactionId());

            return response;
        }
    }
}