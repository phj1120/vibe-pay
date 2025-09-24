package com.vibe.pay.backend.payment.gateway;

import com.vibe.pay.backend.enums.PgCompany;
import com.vibe.pay.backend.payment.*;
import com.vibe.pay.backend.payment.dto.*;
import com.vibe.pay.backend.paymentlog.PaymentInterfaceRequestLog;
import com.vibe.pay.backend.paymentlog.PaymentInterfaceRequestLogMapper;
import com.vibe.pay.backend.util.HashUtils;
import com.vibe.pay.backend.util.WebClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.vibe.pay.backend.common.Constants;
import org.springframework.web.client.RestTemplate;

@Component
public class InicisAdapter implements PaymentGatewayAdapter {

    private static final Logger log = LoggerFactory.getLogger(InicisAdapter.class);

    @Autowired
    private WebClientUtil webClientUtil;
    
    @Autowired
    private PaymentInterfaceRequestLogMapper paymentInterfaceRequestLogMapper;

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${inicis.mid}")
    private String inicisMid;

    @Value("${inicis.apiKey}")
    private String inicisApiKey;

    @Value("${inicis.refundUrl}")
    private String inicisRefundUrl;

    @Value("${inicis.signKey}")
    private String signKey;

    @Value("${inicis.returnUrl}")
    private String returnUrl;

    @Value("${inicis.closeUrl}")
    private String closeUrl;

    @Override
    public PaymentInitResponse initiate(PaymentInitiateRequest request) {
        try {
            log.info("Initiating INICIS payment for order: {}", request.getOrderId());

            // PaymentInitResponse를 모든 필드와 함께 구성
            PaymentInitResponse response = new PaymentInitResponse();
            response.setSuccess(true);
            response.setPaymentUrl("https://stgstdpay.inicis.com/stdpay/pay.ini");

            // Frontend compatibility fields (matching popup.vue form field names)
            String goodName = request.getGoodName();
            String buyerName = request.getBuyerName();
            String buyerTel = request.getBuyerTel();
            String buyerEmail = request.getBuyerEmail();
            String timestamp = LocalDateTime.now().format(Constants.DATETIME_FORMATTER_YYYYMMDDHHMMSS);
            String signature = generateSignature(request.getOrderId(), request.getAmount().toString(), timestamp);
            String verification = generateVerification(request.getOrderId(), request.getAmount().toString(), timestamp);

            response.setMid(inicisMid);
            response.setOid(request.getOrderId());
            response.setPrice(request.getAmount().longValue());
            response.setGoodName(goodName);
            response.setMoId(inicisMid);
            
            // Standard fields (for internal use)
            response.setMerchantId(inicisMid);
            response.setOrderId(request.getOrderId());
            response.setAmount(request.getAmount().toString());
            response.setProductName(goodName);
            response.setBuyerName(buyerName);
            response.setBuyerTel(buyerTel);
            response.setBuyerEmail(buyerEmail);
            response.setTimestamp(timestamp);
            response.setMKey(generateMKey());
            response.setSignature(signature);
            response.setVerification(verification);
            response.setReturnUrl(returnUrl);
            response.setCloseUrl(closeUrl);
            response.setVersion("1.0");
            response.setCurrency("WON");
            response.setGopaymethod("Card");
            response.setAcceptmethod("below1000");
            
            return response;

        } catch (Exception e) {
            log.error("Failed to initiate INICIS payment: {}", e.getMessage(), e);
            PaymentInitResponse errorResponse = new PaymentInitResponse();
            errorResponse.setSuccess(false);
            errorResponse.setErrorMessage("결제 초기화 실패: " + e.getMessage());
            return errorResponse;
        }
    }

    @Override
    public PaymentConfirmResponse confirm(PaymentConfirmRequest request) {
        String orderNumber = null;
        String requestJson = null;
        String responseJson = null;
        String paymentId = request.getPaymentId();

        try {
            // 주문번호 추출 (로그 저장용)
            orderNumber = request.getOrderId();
            if (orderNumber == null || orderNumber.trim().isEmpty()) {
                log.warn("No order ID provided for logging");
            }
            
            String timestamp = String.valueOf(System.currentTimeMillis());
            
            // 이니시스 승인 서명 생성 (SHA-256 - 결제 요청과 동일한 방식)
            String signingText = "authToken=" + request.getAuthToken() + "&timestamp=" + timestamp;
            String signature = HashUtils.sha256Hex(signingText);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("mid", request.getMid());
            params.add("authToken", request.getAuthToken());
            params.add("signature", signature);
            params.add("timestamp", timestamp);
            params.add("charset", "UTF-8");
            params.add("format", "JSON");

            // 실제 요청 정보를 JSON으로 저장
            requestJson = convertToJson(params);

            // WebClient를 사용하여 이니시스 승인 API 호출
            InicisApprovalResponse inicisResponse = webClientUtil.postForm(
                request.getAuthUrl(),
                params,
                InicisApprovalResponse.class
            );
            
            // 실제 응답 정보를 JSON으로 저장
            responseJson = convertToJson(inicisResponse);
            
            log.info("Inicis approval response: {}", inicisResponse);
            
            // 실제 API 호출 로그 저장 - 결제ID 사용
            if (paymentId != null && !paymentId.trim().isEmpty()) {
                PaymentInterfaceRequestLog apiCallLog = new PaymentInterfaceRequestLog(
                        paymentId, // 결제ID 사용
                        "INICIS_API_CALL",
                        requestJson,
                        responseJson
                );
                paymentInterfaceRequestLogMapper.insert(apiCallLog);
            }
            
            // 응답 파싱 및 성공 여부 확인
            boolean success = false;
            String tid = null;
            String responseBodyStr = null;
            
            if (inicisResponse != null) {
                tid = inicisResponse.getTid(); // 거래 ID 추출 (망취소 시 필요)
                responseBodyStr = convertToJson(inicisResponse); // 응답 JSON 문자열
                
                // 승인 성공 조건: resultCode가 "0000"이고 요청 금액과 응답 금액이 일치
                if ("0000".equals(inicisResponse.getResultCode())) {
                    Long requestPrice = request.getPrice();
                    Long responsePrice = inicisResponse.getTotPrice();
                    
                    if (requestPrice != null && requestPrice.equals(responsePrice)) {
                        success = true;
                        log.info("Payment approval successful: tid={}, amount={}",
                                inicisResponse.getTid(), responsePrice);
                    } else {
                        log.error("Payment amount mismatch: requested={}, response={}", 
                                requestPrice, responsePrice);
                    }
                } else {
                    log.error("Payment approval failed: resultCode={}, resultMsg={}", 
                            inicisResponse.getResultCode(), inicisResponse.getResultMsg());
                }
            }
            
            // PaymentConfirmResponse로 변환하여 반환
            PaymentConfirmResponse response = new PaymentConfirmResponse();
            response.setSuccess(success);
            response.setTransactionId(tid);
            response.setAmount(request.getPrice());
            response.setStatus(success ? "SUCCESS" : "FAILED");

            return response;
            
        } catch (Exception e) {
            log.error("Error during Inicis approval process", e);
            
            // 에러 발생 시에도 로그 저장
            if (paymentId != null && !paymentId.trim().isEmpty() && requestJson != null) {
                PaymentInterfaceRequestLog errorLog = new PaymentInterfaceRequestLog(
                        paymentId, // 결제ID 사용
                        "INICIS_API_CALL",
                        requestJson,
                        null // 에러 시 response 없음
                );
                paymentInterfaceRequestLogMapper.insert(errorLog);
            }
            
            PaymentConfirmResponse errorResponse = new PaymentConfirmResponse();
            errorResponse.setSuccess(false);
            errorResponse.setErrorMessage("결제 승인 실패: " + e.getMessage());
            return errorResponse;
        }
    }

    @Override
    public void cancel(PaymentCancelRequest request) {
        try {
            // 타임스탬프 형식: YYYYMMDDhhmmss
            String timestamp = java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

            // 이니시스 취소 요청 파라미터 생성
            InicisRefundRequest refundRequest = new InicisRefundRequest();
            refundRequest.setMid(inicisMid);
            refundRequest.setTimestamp(timestamp);
            refundRequest.setClientIp("127.0.0.1"); // 서버 IP (실제 환경에서는 실제 IP 사용)

            // data 객체 생성
            InicisRefundRequest.RefundData refundData = new InicisRefundRequest.RefundData(
                    request.getTransactionId(), "취소");
            refundRequest.setData(refundData);

            // v2 API 서명 생성: INIAPIKey + " " + mid + " " + type + " " + timestamp + " " + data (SHA512)
            String dataForHash = convertToJson(refundData); // data를 JSON 문자열로 변환
            String signingText = inicisApiKey + inicisMid + refundRequest.getType() + timestamp + dataForHash;
            String hashData = HashUtils.sha512Hex(signingText);

            log.debug("Hash PlainText: {}", signingText);
            log.debug("Hash Text: {}", hashData);
            refundRequest.setHashData(hashData);

            // JSON 요청으로 변경
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            RestTemplate restTemplate = new RestTemplate();

            // 요청 로그 저장
            String requestJson = convertToJson(refundRequest);
            PaymentInterfaceRequestLog requestLog = new PaymentInterfaceRequestLog(
                    request.getPaymentId(),
                    "INICIS_REFUND_REQUEST",
                    requestJson,
                    null
            );
            paymentInterfaceRequestLogMapper.insert(requestLog);

            log.info("Sending INICIS refund request to: {}", inicisRefundUrl);
            log.debug("Request JSON: {}", requestJson);

            // 이니시스 취소 API 호출 (JSON)
            ResponseEntity<InicisRefundResponse> response = restTemplate.postForEntity(
                    inicisRefundUrl,
                    new HttpEntity<>(refundRequest, headers),
                    InicisRefundResponse.class
            );

            InicisRefundResponse refundResponse = response.getBody();
            String responseJson = convertToJson(refundResponse);

            log.info("INICIS refund response: {}", refundResponse);

            // 응답 로그 저장
            PaymentInterfaceRequestLog responseLog = new PaymentInterfaceRequestLog(
                    request.getPaymentId(),
                    "INICIS_REFUND_RESPONSE",
                    requestJson,
                    responseJson
            );
            paymentInterfaceRequestLogMapper.insert(responseLog);

            // 취소 성공 여부 확인
            if (refundResponse != null && "00".equals(refundResponse.getResultCode())) {
                log.info("INICIS refund successful: TID={}, CancelDate={}",
                        refundResponse.getTid(), refundResponse.getCancelDate());
            } else {
                String errorMsg = refundResponse != null ?
                        refundResponse.getResultMsg() : "Unknown error";
                log.error("INICIS refund failed: Code={}, Message={}",
                        refundResponse != null ? refundResponse.getResultCode() : "NULL", errorMsg);
                throw new RuntimeException("INICIS refund failed: " + errorMsg);
            }

        } catch (Exception e) {
            // 에러 로그 저장
            PaymentInterfaceRequestLog errorLog = new PaymentInterfaceRequestLog(
                    request.getPaymentId(),
                    "INICIS_REFUND_ERROR",
                    "TID: " + request.getTransactionId(),
                    "ERROR: " + e.getMessage()
            );
            paymentInterfaceRequestLogMapper.insert(errorLog);

            throw new RuntimeException("INICIS refund API call failed", e);
        }
    }

    @Override
    public void netCancel(PaymentNetCancelRequest request) {
        String netCancelUrl = request.getNetCancelUrl();
        String authToken = request.getAuthToken();
        String orderNumber = request.getOrderNumber();

        try {
            if (netCancelUrl == null || netCancelUrl.isBlank()) {
                log.warn("Cannot perform net cancel - netCancelUrl is null or blank");
                return;
            }

            if (authToken == null || authToken.isBlank()) {
                log.warn("Cannot perform net cancel - authToken is null or blank");
                return;
            }

            String timestamp = String.valueOf(System.currentTimeMillis());
            // 망취소 서명 생성 (SHA-256 - 결제 요청과 동일한 방식)
            String signingText = "authToken=" + authToken + "&timestamp=" + timestamp;
            String signature = HashUtils.sha256Hex(signingText);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("mid", inicisMid);
            params.add("authToken", authToken);
            params.add("signature", signature);
            params.add("timestamp", timestamp);
            params.add("charset", "UTF-8");
            params.add("format", "JSON");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            RestTemplate rt = new RestTemplate();
            ResponseEntity<String> resp = rt.postForEntity(netCancelUrl, new HttpEntity<>(params, headers), String.class);

            // 망취소 로그 저장 (PaymentInterfaceRequestLog에 정식 기록)
            String requestJson = convertToJson(params);
            String responseJson = convertToJson(resp);

            PaymentInterfaceRequestLog netCancelLog = new PaymentInterfaceRequestLog(
                    orderNumber,
                    "NET_CANCEL_REQUEST",
                    requestJson,
                    responseJson
            );
            paymentInterfaceRequestLogMapper.insert(netCancelLog);

            log.info("Net cancel completed for order: {}, response status: {}",
                    orderNumber, resp.getStatusCode());

        } catch (Exception e) {
            log.error("Error during net cancel for order: {}", orderNumber, e);

            // 에러 로그도 기록
            try {
                PaymentInterfaceRequestLog errorLog = new PaymentInterfaceRequestLog(
                        orderNumber,
                        "NET_CANCEL_ERROR",
                        "Order: " + orderNumber + ", Error: " + e.getMessage(),
                        null
                );
                paymentInterfaceRequestLogMapper.insert(errorLog);
            } catch (Exception logException) {
                log.error("Failed to log net cancel error", logException);
            }

            // 예외를 다시 던짐
            throw new RuntimeException("Net cancel failed for order: " + orderNumber, e);
        }
    }

    @Override
    public boolean supports(String pgCompany) {
        return PgCompany.INICIS.getCode().equals(pgCompany);
    }

    private String generateSignature(String orderId, String amount, String timestamp) {
        String signingText = "oid=" + orderId
                + "&price=" + amount
                + "&timestamp=" + timestamp;
        return HashUtils.sha256Hex(signingText);
    }

    private String generateMKey() {
        return HashUtils.sha256Hex(signKey);
    }

    private String generateVerification(String orderId, String amount, String timestamp) {
        String data = "oid=" + orderId + "&price=" + amount + "&signKey=" + signKey + "&timestamp=" + timestamp;
        return HashUtils.sha256Hex(data);
    }

    private String convertToJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("Failed to convert object to JSON: {}", e.getMessage());
            return obj.toString();
        }
    }
}