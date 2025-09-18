package com.vibe.pay.backend.payment;

import com.vibe.pay.backend.paymentlog.PaymentInterfaceRequestLog;
import com.vibe.pay.backend.paymentlog.PaymentInterfaceRequestLogMapper;
import com.vibe.pay.backend.order.OrderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.security.MessageDigest;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.MultiValueMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;


@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private PaymentInterfaceRequestLogMapper paymentInterfaceRequestLogMapper;

    @Autowired
    private OrderMapper orderMapper;


    @Autowired
    private ObjectMapper objectMapper;

    // 환경별 설정 주입
    @Value("${inicis.mid}")
    private String inicisMid;

    @Value("${inicis.signKey}")
    private String inicisSignKey;

    @Value("${inicis.returnUrl}")
    private String inicisReturnUrl;

    @Value("${inicis.closeUrl}")
    private String inicisCloseUrl;

    @Value("${inicis.currency}")
    private String inicisCurrency;

    @Value("${inicis.version}")
    private String inicisVersion;

    @Value("${inicis.gopaymethod}")
    private String inicisGopaymethod;

    @Value("${inicis.acceptmethod}")
    private String inicisAcceptmethod;

    public Payment createPayment(Payment payment) {
        paymentMapper.insert(payment);
        return payment;
    }

    public Optional<Payment> getPaymentById(Long id) {
        return Optional.ofNullable(paymentMapper.findById(id));
    }

    public List<Payment> getAllPayments() {
        return paymentMapper.findAll();
    }

    public Payment updatePayment(Long id, Payment paymentDetails) {
        Payment existingPayment = paymentMapper.findById(id);
        if (existingPayment == null) {
            throw new RuntimeException("Payment not found with id " + id);
        }
        paymentDetails.setId(id);
        paymentMapper.update(paymentDetails);
        return paymentDetails;
    }

    public void deletePayment(Long id) {
        paymentMapper.delete(id);
    }

    @Transactional
    public InicisPaymentParameters initiatePayment(PaymentInitiateRequest request) {
        // 결제 시작 로그
        log.info("Initiating payment: memberId={}, amount={}, method={}, usedMileage={}", 
                request.getMemberId(), request.getAmount(), request.getPaymentMethod(), request.getUsedMileage());
        log.info("Request details: goodName={}, buyerName={}, buyerTel={}, buyerEmail={}", 
                request.getGoodName(), request.getBuyerName(), request.getBuyerTel(), request.getBuyerEmail());

        // PG 선택 - 현재는 INICIS만 지원 (추후 NICEPAY 추가 예정)
        String pgCompany = "INICIS";
        log.debug("Selected PG company: {}", pgCompany);

        // 결제 방법 결정 (적립금 사용 여부에 따라)
        String paymentMethod = request.getPaymentMethod();
        // 적립금 사용량이 있다면 mileage도 포함
        if (request.getUsedMileage() != null && request.getUsedMileage() > 0) {
            paymentMethod = paymentMethod + "+MILEAGE";
        }

        // 결제 프로세스 시작: 주문번호 채번 먼저
        // 실제 주문번호 채번
        Long orderNumber = orderMapper.getNextOrderSequence();
        log.info("Generated order number: {}", orderNumber);

        // 인터페이스 로그 먼저 기록 (결제 요청 시작) - 주문번호로 로그 저장
        String requestJson = convertToJson(request);
        
        PaymentInterfaceRequestLog initiateLog = new PaymentInterfaceRequestLog(
                orderNumber, // 주문번호 사용
                "INITIATE_PAYMENT",
                requestJson,
                null // response는 없음
        );
        paymentInterfaceRequestLogMapper.insert(initiateLog);

        // 이니시스 파라미터 구성 - 화면에서 넘어온 값 사용
        InicisPaymentParameters inicisParams = new InicisPaymentParameters();
        inicisParams.setMid(inicisMid);

        // 채번된 주문번호를 OID로 직접 사용
        String oid = String.valueOf(orderNumber);
        inicisParams.setOid(oid);

        // price: 요청 금액을 정수 문자열로 변환
        String priceStr = "0";
        if (request.getAmount() != null) {
            BigDecimal amt = BigDecimal.valueOf(request.getAmount()).setScale(0, RoundingMode.HALF_UP);
            priceStr = amt.toPlainString();
        }
        inicisParams.setPrice(priceStr);

        // 화면에서 넘어온 표기 정보 (한글 허용, 특수문자만 제거)
        log.info("Original request data - goodName: {}, buyerName: {}, buyerTel: {}, buyerEmail: {}", 
                request.getGoodName(), request.getBuyerName(), request.getBuyerTel(), request.getBuyerEmail());
        
        inicisParams.setGoodName(sanitizeForInicis(request.getGoodName(), "주문결제"));
        inicisParams.setBuyerName(sanitizeForInicis(request.getBuyerName(), "구매자"));
        inicisParams.setBuyerTel(sanitizeForPhoneNumber(request.getBuyerTel()));
        inicisParams.setBuyerEmail(sanitizeForInicis(request.getBuyerEmail(), "buyer@example.com"));
        
        log.info("Sanitized data - goodName: {}, buyerName: {}, buyerTel: {}, buyerEmail: {}", 
                inicisParams.getGoodName(), inicisParams.getBuyerName(), inicisParams.getBuyerTel(), inicisParams.getBuyerEmail());

        // 환경설정 기반 옵션
        inicisParams.setVersion(inicisVersion);
        inicisParams.setCurrency(inicisCurrency);
        inicisParams.setGopaymethod(inicisGopaymethod);
        inicisParams.setAcceptmethod(inicisAcceptmethod);

        // 타임스탬프(ms) - 이니시스 시그니처 생성용
        String timestamp = String.valueOf(System.currentTimeMillis());
        inicisParams.setTimestamp(timestamp);

        // signKey 정제
        log.info("Checking Inicis configuration - MID: {}, SignKey present: {}", inicisMid, inicisSignKey != null);
        final String finalSignKey = (inicisSignKey != null) ? inicisSignKey.trim() : null;
        if (finalSignKey == null || finalSignKey.isBlank()) {
            log.error("Inicis signKey is not configured. Set INICIS_SIGN_KEY env/property for MID={}", inicisMid);
            throw new RuntimeException("INICIS signKey not configured");
        }

        // 단일 SHA-256 메서드로 mKey/Signature/Verification 생성
        inicisParams.setmKey(sha256Hex(finalSignKey));

        String signingText = "oid=" + inicisParams.getOid()
                + "&price=" + inicisParams.getPrice()
                + "&timestamp=" + inicisParams.getTimestamp();
        inicisParams.setSignature(sha256Hex(signingText));

        String verificationText = "oid=" + inicisParams.getOid()
                + "&price=" + inicisParams.getPrice()
                + "&signKey=" + finalSignKey
                + "&timestamp=" + inicisParams.getTimestamp();
        inicisParams.setVerification(sha256Hex(verificationText));

        // 환경설정 기반 콜백 URL
        inicisParams.setReturnUrl(inicisReturnUrl);
        inicisParams.setCloseUrl(inicisCloseUrl);

        // 인터페이스 로그 업데이트 - 이니시스 파라미터 생성 완료
        String inicisParamsJson = convertToJson(inicisParams);
        
        PaymentInterfaceRequestLog parametersLog = new PaymentInterfaceRequestLog(
                orderNumber, // 주문번호 사용
                "INICIS_PARAMETERS_GENERATED",
                inicisParamsJson,
                null // response는 없음
        );
        paymentInterfaceRequestLogMapper.insert(parametersLog);

        log.info("Payment initiation parameters issued for order={}, oid={}", orderNumber, oid);
        return inicisParams;
    }

    @Transactional
    public Payment confirmPayment(PaymentConfirmRequest request) {
        log.info("Processing payment confirmation: {}", request);
        log.info("Request details - authToken: {}, authUrl: {}, orderNumber: {}, price: {}", 
                request.getAuthToken(), request.getAuthUrl(), request.getOrderNumber(), request.getPrice());
        
        // 주문번호 추출 (orderNumber 필드 사용)
        String orderNumberStr = request.getOrderNumber();
        Long orderNumber = null;
        
        if (orderNumberStr != null) {
            try {
                // orderNumber가 주문번호 자체이므로 바로 파싱
                orderNumber = Long.parseLong(orderNumberStr);
                log.info("Extracted order number: {}", orderNumber);
            } catch (Exception e) {
                log.error("Failed to parse order number: {}", orderNumberStr, e);
                throw new RuntimeException("Invalid order number format: " + orderNumberStr);
            }
        }

        if (orderNumber == null) {
            throw new RuntimeException("Order number is required");
        }

        // 승인 요청 인터페이스 로그 먼저 기록
        String approvalRequestJson = convertToJson(request);
        
        PaymentInterfaceRequestLog approvalRequestLog = new PaymentInterfaceRequestLog(
                orderNumber, // 주문번호 사용
                "INICIS_APPROVAL_REQUEST",
                approvalRequestJson,
                null // response는 없음
        );
        paymentInterfaceRequestLogMapper.insert(approvalRequestLog);

        // 이니시스 승인 처리
        boolean approvalSuccess = false;
        String transactionId = null;
        String actualResponse = null;
        
        if (request.getAuthToken() != null && request.getAuthUrl() != null) {
                log.info("Starting Inicis approval process for order: {}", orderNumber);
            try {
                // 실제 이니시스 승인 API 호출
                ApprovalResult result = processInicisApprovalWithResponse(request);
                approvalSuccess = result.isSuccess();
                actualResponse = result.getResponseBody();
                
                if (approvalSuccess) {
                    // 이니시스에서 받은 실제 거래 ID 사용
                    transactionId = result.getTransactionId();
                    log.info("Payment approved successfully for order: {}, transactionId: {}", orderNumber, transactionId);
                } else {
                    log.error("Payment approval failed for order: {}", orderNumber);
                    // 승인 실패 시 망취소 시도
                    if (request.getNetCancelUrl() != null) {
                        log.info("Attempting net cancel due to approval failure for order: {}", orderNumber);
                        safeNetCancel(request.getNetCancelUrl(), request.getAuthToken());
                    }
                }
            } catch (Exception e) {
                log.error("Error during Inicis approval process for order: {}", orderNumber, e);
                // 에러 발생 시 망취소 시도
                if (request.getNetCancelUrl() != null) {
                    log.info("Attempting net cancel due to error for order: {}", orderNumber);
                    safeNetCancel(request.getNetCancelUrl(), request.getAuthToken());
                }
            }
        }

        // 승인 응답 인터페이스 로그 기록 - 실제 응답값 저장
        String summaryRequestJson = convertToJson(request);
        
        PaymentInterfaceRequestLog approvalResponseLog = new PaymentInterfaceRequestLog(
                orderNumber, // 주문번호 사용
                "INICIS_APPROVAL_RESPONSE",
                summaryRequestJson,
                actualResponse // 실제 이니시스 응답 또는 null
        );
        paymentInterfaceRequestLogMapper.insert(approvalResponseLog);

        // 성공한 경우에만 Payment 테이블에 저장
        Payment payment = null;
        if (approvalSuccess) {
            // 주문번호를 기반으로 원래 결제 요청 정보를 찾기 위해 인터페이스 로그에서 조회
            Long memberId = extractMemberIdFromLogs(orderNumber);
            String paymentMethod = extractPaymentMethodFromLogs(orderNumber);
            
            payment = new Payment(
                    memberId, // 실제 memberId 사용
                    Double.valueOf(request.getPrice()),
                    paymentMethod,
                    "INICIS",
                    "SUCCESS",
                    transactionId
            );
            paymentMapper.insert(payment);
            
            log.info("Payment record created successfully: paymentId={}, memberId={}, order={}", 
                    payment.getId(), memberId, orderNumber);
        } else {
            // 실패한 경우 Payment 테이블에는 저장하지 않음
            log.info("Payment failed, no record created in Payment table for order: {}", orderNumber);
            throw new RuntimeException("Payment approval failed");
        }

        return payment;
    }
    
    // 승인 결과를 담는 내부 클래스
    private static class ApprovalResult {
        private final boolean success;
        private final String responseBody;
        private final String transactionId;

        public ApprovalResult(boolean success, String responseBody, String transactionId) {
            this.success = success;
            this.responseBody = responseBody;
            this.transactionId = transactionId;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getResponseBody() {
            return responseBody;
        }

        public String getTransactionId() {
            return transactionId;
        }
    }
    
    private ApprovalResult processInicisApprovalWithResponse(PaymentConfirmRequest request) {
        Long orderNumber = null;
        String requestJson = null;
        String responseJson = null;
        
        try {
            // 주문번호 추출 (로그 저장용)
            String orderNumberStr = request.getOrderNumber();
            if (orderNumberStr != null) {
                try {
                    // orderNumber가 주문번호 자체이므로 바로 파싱
                    orderNumber = Long.parseLong(orderNumberStr);
                } catch (Exception e) {
                    log.warn("Failed to extract order number for logging: {}", orderNumberStr);
                }
            }
            
            String timestamp = String.valueOf(System.currentTimeMillis());
            
            // 이니시스 승인 서명 생성 (SHA-256 - 결제 요청과 동일한 방식)
            String signingText = "authToken=" + request.getAuthToken() + "&timestamp=" + timestamp;
            String signature = sha256Hex(signingText);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("mid", request.getMid() != null ? request.getMid() : inicisMid);
            params.add("authToken", request.getAuthToken());
            params.add("signature", signature);
            params.add("timestamp", timestamp);
            params.add("charset", "UTF-8");
            params.add("format", "JSON");

            // 실제 요청 정보를 JSON으로 저장
            requestJson = convertToJson(params);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            RestTemplate rt = new RestTemplate();

            ResponseEntity<String> response = rt.postForEntity(request.getAuthUrl(), new HttpEntity<>(params, headers), String.class);
            String responseBody = response.getBody();
            
            // 실제 응답 정보를 JSON으로 저장
            responseJson = convertToJson(response);
            
            log.info("Inicis approval response: {}", responseBody);
            
            // 실제 API 호출 로그 저장
            if (orderNumber != null) {
                PaymentInterfaceRequestLog apiCallLog = new PaymentInterfaceRequestLog(
                        orderNumber, // 주문번호를 ID로 사용
                        "INICIS_API_CALL",
                        requestJson,
                        responseJson
                );
                paymentInterfaceRequestLogMapper.insert(apiCallLog);
            }
            
            // 응답 파싱 및 성공 여부 확인
            boolean success = false;
            String tid = null;
            if (responseBody != null && !responseBody.trim().isEmpty()) {
                try {
                    InicisApprovalResponse inicisResponse = objectMapper.readValue(responseBody, InicisApprovalResponse.class);
                    tid = inicisResponse.getTid(); // 거래 ID 추출 (망취소 시 필요)
                    
                    // 승인 성공 조건: resultCode가 "0000"이고 요청 금액과 응답 금액이 일치
                    if ("0000".equals(inicisResponse.getResultCode())) {
                        String requestPrice = request.getPrice();
                        String responsePrice = inicisResponse.getTotPrice();
                        
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
                } catch (Exception e) {
                    log.error("Failed to parse Inicis response: {}", responseBody, e);
                    // JSON 파싱 실패 시 기존 방식으로 폴백
                    success = responseBody.contains("\"resultCode\":\"0000\"");
                }
            }
            
            return new ApprovalResult(success, responseBody, tid);
            
        } catch (Exception e) {
            log.error("Error during Inicis approval process", e);
            
            // 에러 발생 시에도 로그 저장
            if (orderNumber != null && requestJson != null) {
                PaymentInterfaceRequestLog errorLog = new PaymentInterfaceRequestLog(
                        orderNumber,
                        "INICIS_API_CALL",
                        requestJson,
                        null // 에러 시 response 없음
                );
                paymentInterfaceRequestLogMapper.insert(errorLog);
            }
            
            return new ApprovalResult(false, null, null);
        }
    }
    

    @Transactional
    public Payment processPayment(Payment payment) {
        // 실제 PG 연동 로직이 필요한 부분
        // 현재는 이니시스만 지원하므로 해당 로직으로 리다이렉트
        log.warn("processPayment() called - this should use confirmPayment() for actual PG integration");
        
        // 상태 업데이트만 수행 (실제 PG 연동은 confirmPayment에서 처리)
        paymentMapper.update(payment);
        return payment;
    }

    @Transactional
    public Payment cancelPayment(Long paymentId) {
        Payment payment = paymentMapper.findById(paymentId);
        if (payment == null) {
            throw new RuntimeException("Payment not found with id " + paymentId);
        }

        if ("CANCELLED".equals(payment.getStatus())) {
            throw new RuntimeException("Payment is already cancelled.");
        }

        // 실제 PG 취소 로직이 필요한 부분
        // TODO: 이니시스 취소 API 연동 구현 필요
        log.warn("Cancellation requested for payment ID: {} with PG: {} - actual PG cancel API not implemented yet", 
                paymentId, payment.getPgCompany());

        // Update payment status
        payment.setStatus("CANCELLED");
        // transactionId는 그대로 유지 (임의로 변경하지 않음)
        paymentMapper.update(payment);

        // Create PaymentInterfaceRequestLog entry for cancellation (실제 객체만 저장)
        PaymentInterfaceRequestLog log = new PaymentInterfaceRequestLog(
                payment.getId(),
                "CANCEL_PAYMENT",
                convertToJson(payment), // 실제 payment 객체
                null // 취소 시 별도 response 없음
        );
        paymentInterfaceRequestLogMapper.insert(log);

        // In a real scenario, you might also update the associated Order status here
        // or handle it in OrderService.cancelOrder
        // For now, OrderService.cancelOrder will handle the order status update.

        return payment;
    }


        private void safeNetCancel(String netCancelUrl, String authToken) {
            try {
                if (netCancelUrl == null || netCancelUrl.isBlank()) return;
                String timestamp = String.valueOf(System.currentTimeMillis());
                // 망취소 서명 생성 (SHA-256 - 결제 요청과 동일한 방식)
                String signingText = "authToken=" + authToken + "&timestamp=" + timestamp;
                String signature = sha256Hex(signingText);

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
                
                // 망취소 로그 저장 (실제 객체만)
                log.warn("NetCancel called. Request={}, Response={}", 
                        convertToJson(params), 
                        convertToJson(resp));
                
            } catch (Exception e) {
                log.error("Error during net cancel", e);
            }
        }


    private static String sha256Hex(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(text.getBytes(StandardCharsets.UTF_8));
            return String.format("%064x", new BigInteger(1, md.digest()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute SHA-256", e);
        }
    }
    
    /**
     * 이니시스 파라미터를 위한 문자열 정리
     * 한글 허용, 이니시스에서 문제가 될 수 있는 특수문자만 제거
     */
    private String sanitizeForInicis(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        
        String sanitized = value.trim();
        
        // 이니시스에서 문제가 될 수 있는 특수문자 제거
        // 허용: 한글, 영문, 숫자, 공백, @, -, ., (, ), /
        // 제거: <, >, &, ", ', ;, =, +, *, %, #, $, ^, ~, `, |, \, {, }, [, ]
        sanitized = sanitized.replaceAll("[<>\"';&=+*%#$^~`|\\\\{}\\[\\]]", "");
        
        // 연속된 공백을 하나로 변경
        sanitized = sanitized.replaceAll("\\s+", " ");
        
        // 앞뒤 공백 제거
        sanitized = sanitized.trim();
        
        // 빈 문자열이 되면 기본값 사용
        if (sanitized.isEmpty()) {
            return defaultValue;
        }
        
        // 길이 제한 (이니시스 파라미터 길이 제한 고려)
        if (sanitized.length() > 100) {
            sanitized = sanitized.substring(0, 97) + "...";
        }
        
        return sanitized;
    }
    
    /**
     * 전화번호 전용 정리 메서드
     * 숫자, -, (, ), 공백만 허용
     */
    private String sanitizeForPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return "010-0000-0000";
        }
        
        String sanitized = phoneNumber.trim();
        
        // 전화번호에서 허용되는 문자만 남기기: 숫자, -, (, ), 공백
        sanitized = sanitized.replaceAll("[^0-9\\-\\(\\)\\s]", "");
        
        // 연속된 공백을 하나로 변경
        sanitized = sanitized.replaceAll("\\s+", " ");
        
        // 앞뒤 공백 제거
        sanitized = sanitized.trim();
        
        // 빈 문자열이 되면 기본값 사용
        if (sanitized.isEmpty()) {
            return "010-0000-0000";
        }
        
        // 전화번호 형식 검증 (간단한 검증)
        if (!sanitized.matches(".*[0-9].*")) {
            log.warn("Invalid phone number format: {}, using default", phoneNumber);
            return "010-0000-0000";
        }
        
        // 길이 제한
        if (sanitized.length() > 20) {
            sanitized = sanitized.substring(0, 20);
        }
        
        return sanitized;
    }

    // JSON 변환 유틸리티 메소드
    private String convertToJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert object to JSON: {}", obj, e);
            return "{}";
        }
    }

    // 로그에서 memberId 추출 (JSON 형식에서)
    private Long extractMemberIdFromLogs(Long orderNumber) {
        try {
            List<PaymentInterfaceRequestLog> logs = paymentInterfaceRequestLogMapper.findByPaymentId(orderNumber);
            for (PaymentInterfaceRequestLog log : logs) {
                if ("INITIATE_PAYMENT".equals(log.getRequestType())) {
                    String payload = log.getRequestPayload();
                    // JSON 형태에서 memberId 추출: {"memberId":1,"amount":10000,...}
                    if (payload != null && payload.contains("\"memberId\":")) {
                        // 간단한 JSON 파싱 (정규식 사용)
                        String memberIdStr = payload.replaceAll(".*\"memberId\":(\\d+).*", "$1");
                        return Long.parseLong(memberIdStr);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to extract memberId from logs for order: {}", orderNumber, e);
        }
        
        // 기본값 대신 예외 발생
        throw new RuntimeException("Cannot extract memberId from payment logs for order: " + orderNumber);
    }

    // 로그에서 paymentMethod 추출 (JSON 형식에서)
    private String extractPaymentMethodFromLogs(Long orderNumber) {
        try {
            List<PaymentInterfaceRequestLog> logs = paymentInterfaceRequestLogMapper.findByPaymentId(orderNumber);
            for (PaymentInterfaceRequestLog log : logs) {
                if ("INITIATE_PAYMENT".equals(log.getRequestType())) {
                    String payload = log.getRequestPayload();
                    // JSON 형태에서 paymentMethod 추출: {"paymentMethod":"CREDIT_CARD",...}
                    if (payload != null && payload.contains("\"paymentMethod\":")) {
                        // 간단한 JSON 파싱 (정규식 사용)
                        String methodStr = payload.replaceAll(".*\"paymentMethod\":\"([^\"]+)\".*", "$1");
                        return methodStr;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to extract paymentMethod from logs for order: {}", orderNumber, e);
        }
        
        // 기본값 대신 예외 발생
        throw new RuntimeException("Cannot extract paymentMethod from payment logs for order: " + orderNumber);
    }
}