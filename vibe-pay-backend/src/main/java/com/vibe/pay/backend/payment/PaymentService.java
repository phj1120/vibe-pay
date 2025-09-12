package com.vibe.pay.backend.payment;

import com.vibe.pay.backend.paymentlog.PaymentInterfaceRequestLog;
import com.vibe.pay.backend.paymentlog.PaymentInterfaceRequestLogMapper;
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

        // 결제 레코드는 나중에 성공시에만 생성하도록 변경
        // 임시로 payment ID 생성을 위해 현재 시간 사용
        Long tempPaymentId = System.currentTimeMillis();

        // 인터페이스 로그 먼저 기록 (결제 요청 시작) - JSON 형태로 저장
        String requestJson = convertToJson(request);
        
        PaymentInterfaceRequestLog initiateLog = new PaymentInterfaceRequestLog(
                tempPaymentId, // 임시 ID 사용
                "INITIATE_PAYMENT",
                requestJson,
                null // response는 없음
        );
        paymentInterfaceRequestLogMapper.insert(initiateLog);

        // 이니시스 파라미터 구성 - 화면에서 넘어온 값 사용
        InicisPaymentParameters inicisParams = new InicisPaymentParameters();
        inicisParams.setMid(inicisMid);

        // 주문 번호가 있으면 사용, 없으면 임시 ID 기반 OID 생성
        String oid;
        if (request.getOrderId() != null) {
            // 기존 주문이 있는 경우 (기존 프로세스)
            oid = "OID-" + request.getOrderId() + "-" + System.currentTimeMillis();
        } else {
            // 새로운 프로세스: 주문 번호 채번 후 결제 요청
            oid = "OID-" + tempPaymentId + "-" + System.currentTimeMillis();
        }
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
        inicisParams.setMoId(String.valueOf(tempPaymentId));
        inicisParams.setGopaymethod(inicisGopaymethod);
        inicisParams.setAcceptmethod(inicisAcceptmethod);

        // 타임스탬프(ms)
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

        // 인터페이스 로그 업데이트 - 이니시스 파라미터 생성 완료 (JSON 형태로 저장)
        String inicisParamsJson = convertToJson(inicisParams);
        
        PaymentInterfaceRequestLog parametersLog = new PaymentInterfaceRequestLog(
                tempPaymentId,
                "INICIS_PARAMETERS_GENERATED",
                inicisParamsJson,
                null // response는 없음
        );
        paymentInterfaceRequestLogMapper.insert(parametersLog);

        log.info("Payment initiation parameters issued for tempId={}, oid={}", tempPaymentId, oid);
        return inicisParams;
    }

    @Transactional
    public Payment confirmPayment(PaymentConfirmRequest request) {
        log.info("Processing payment confirmation: {}", request);
        log.info("Request details - authToken: {}, authUrl: {}, oid: {}, price: {}", 
                request.getAuthToken(), request.getAuthUrl(), request.getOid(), request.getPrice());
        
        // OID에서 tempPaymentId 추출
        String oidToSearch = request.getOrderNumber() != null ? request.getOrderNumber() : request.getOid();
        Long tempPaymentId = null;
        if (oidToSearch != null) {
            try {
                String[] oidParts = oidToSearch.split("-");
                if (oidParts.length >= 2) {
                    tempPaymentId = Long.parseLong(oidParts[1]);
                }
            } catch (Exception e) {
                log.error("Failed to extract tempPaymentId from OID: {}", oidToSearch, e);
                throw new RuntimeException("Invalid OID format: " + oidToSearch);
            }
        }

        if (tempPaymentId == null) {
            throw new RuntimeException("Cannot extract tempPaymentId from OID: " + request.getOid());
        }

        // 승인 요청 인터페이스 로그 먼저 기록 (JSON 형태로 저장)
        String approvalRequestJson = convertToJson(request);
        
        PaymentInterfaceRequestLog approvalRequestLog = new PaymentInterfaceRequestLog(
                tempPaymentId,
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
            log.info("Starting Inicis approval process for tempId: {}", tempPaymentId);
            try {
                // 실제 이니시스 승인 API 호출
                ApprovalResult result = processInicisApprovalWithResponse(request);
                approvalSuccess = result.isSuccess();
                actualResponse = result.getResponseBody();
                
                if (approvalSuccess) {
                    transactionId = "TXN-" + System.currentTimeMillis();
                    log.info("Payment approved successfully for tempId: {}", tempPaymentId);
                } else {
                    log.error("Payment approval failed for tempId: {}", tempPaymentId);
                }
            } catch (Exception e) {
                log.error("Error during Inicis approval process for tempId: {}", tempPaymentId, e);
                // 망취소 시도
                if (request.getNetCancelUrl() != null) {
                    log.info("Attempting net cancel for tempId: {}", tempPaymentId);
                    safeNetCancel(request.getNetCancelUrl(), request.getAuthToken());
                }
            }
        }

        // 승인 응답 인터페이스 로그 기록 - 실제 응답값 저장
        String summaryRequestJson = convertToJson(request);
        
        PaymentInterfaceRequestLog approvalResponseLog = new PaymentInterfaceRequestLog(
                tempPaymentId,
                "INICIS_APPROVAL_RESPONSE",
                summaryRequestJson,
                actualResponse // 실제 이니시스 응답 또는 null
        );
        paymentInterfaceRequestLogMapper.insert(approvalResponseLog);

        // 성공한 경우에만 Payment 테이블에 저장
        Payment payment = null;
        if (approvalSuccess) {
            // tempPaymentId를 기반으로 원래 결제 요청 정보를 찾기 위해 인터페이스 로그에서 조회
            Long memberId = extractMemberIdFromLogs(tempPaymentId);
            String paymentMethod = extractPaymentMethodFromLogs(tempPaymentId);
            
            payment = new Payment(
                    memberId, // 실제 memberId 사용
                    Double.valueOf(request.getPrice()),
                    paymentMethod,
                    "INICIS",
                    "SUCCESS",
                    transactionId
            );
            paymentMapper.insert(payment);
            
            log.info("Payment record created successfully: paymentId={}, memberId={}, tempId={}", 
                    payment.getId(), memberId, tempPaymentId);
        } else {
            // 실패한 경우 Payment 테이블에는 저장하지 않음
            log.info("Payment failed, no record created in Payment table for tempId: {}", tempPaymentId);
            throw new RuntimeException("Payment approval failed");
        }

        return payment;
    }
    
    // 승인 결과를 담는 내부 클래스
    private static class ApprovalResult {
        private final boolean success;
        private final String responseBody;
        
        public ApprovalResult(boolean success, String responseBody) {
            this.success = success;
            this.responseBody = responseBody;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getResponseBody() {
            return responseBody;
        }
    }
    
    private ApprovalResult processInicisApprovalWithResponse(PaymentConfirmRequest request) {
        Long tempPaymentId = null;
        String requestJson = null;
        String responseJson = null;
        
        try {
            // OID에서 tempPaymentId 추출 (로그 저장용)
            String oidToSearch = request.getOrderNumber() != null ? request.getOrderNumber() : request.getOid();
            if (oidToSearch != null) {
                try {
                    String[] oidParts = oidToSearch.split("-");
                    if (oidParts.length >= 2) {
                        tempPaymentId = Long.parseLong(oidParts[1]);
                    }
                } catch (Exception e) {
                    log.warn("Failed to extract tempPaymentId from OID for logging: {}", oidToSearch);
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
            if (tempPaymentId != null) {
                PaymentInterfaceRequestLog apiCallLog = new PaymentInterfaceRequestLog(
                        tempPaymentId,
                        "INICIS_API_CALL",
                        requestJson,
                        responseJson
                );
                paymentInterfaceRequestLogMapper.insert(apiCallLog);
            }
            
            // 응답에서 성공 여부 확인
            boolean success = responseBody != null && responseBody.contains("\"resultCode\":\"0000\"");
            
            return new ApprovalResult(success, responseBody);
            
        } catch (Exception e) {
            log.error("Error during Inicis approval process", e);
            
            // 에러 발생 시에도 로그 저장
            if (tempPaymentId != null && requestJson != null) {
                PaymentInterfaceRequestLog errorLog = new PaymentInterfaceRequestLog(
                        tempPaymentId,
                        "INICIS_API_CALL",
                        requestJson,
                        null // 에러 시 response 없음
                );
                paymentInterfaceRequestLogMapper.insert(errorLog);
            }
            
            return new ApprovalResult(false, null);
        }
    }
    

    @Transactional
    public Payment processPayment(Payment payment) {
        // This is a placeholder for actual PG integration logic
        // In a real scenario, you would call a PG API here
        System.out.println("Processing payment for amount: " + payment.getAmount() + " with PG: " + payment.getPgCompany());
        payment.setStatus("APPROVED"); // Simulate approval
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

        // Simulate PG cancellation
        System.out.println("Simulating PG cancellation for payment ID: " + paymentId + " with PG: " + payment.getPgCompany());

        // Update payment status
        payment.setStatus("CANCELLED");
        payment.setTransactionId(payment.getTransactionId() + "-CANCEL"); // Append -CANCEL for simulation
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
    private Long extractMemberIdFromLogs(Long tempPaymentId) {
        try {
            List<PaymentInterfaceRequestLog> logs = paymentInterfaceRequestLogMapper.findByPaymentId(tempPaymentId);
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
            log.error("Failed to extract memberId from logs for tempId: {}", tempPaymentId, e);
        }
        return 1L; // 기본값
    }

    // 로그에서 paymentMethod 추출 (JSON 형식에서)
    private String extractPaymentMethodFromLogs(Long tempPaymentId) {
        try {
            List<PaymentInterfaceRequestLog> logs = paymentInterfaceRequestLogMapper.findByPaymentId(tempPaymentId);
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
            log.error("Failed to extract paymentMethod from logs for tempId: {}", tempPaymentId, e);
        }
        return "CREDIT_CARD"; // 기본값
    }
}