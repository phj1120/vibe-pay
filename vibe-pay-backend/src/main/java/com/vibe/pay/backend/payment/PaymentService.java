package com.vibe.pay.backend.payment;

import com.vibe.pay.backend.paymentlog.PaymentInterfaceRequestLog;
import com.vibe.pay.backend.paymentlog.PaymentInterfaceRequestLogMapper;
import com.vibe.pay.backend.order.OrderMapper;
import com.vibe.pay.backend.pointhistory.PointHistoryService;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    private PointHistoryService pointHistoryService;

    @Autowired
    private ObjectMapper objectMapper;

    // 환경별 설정 주입
    @Value("${inicis.mid}")
    private String inicisMid;

    @Value("${inicis.signKey}")
    private String inicisSignKey;

    @Value("${inicis.apiKey}")
    private String inicisApiKey;

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

    @Value("${inicis.refundUrl}")
    private String inicisRefundUrl;

    public Payment createPayment(Payment payment) {
        // Generate payment ID if not already set
        if (payment.getPaymentId() == null) {
            String paymentId = generatePaymentId();
            payment.setPaymentId(paymentId);
        }
        paymentMapper.insert(payment);
        return payment;
    }

    public Optional<Payment> getPaymentById(String paymentId) {
        return Optional.ofNullable(paymentMapper.findByPaymentId(paymentId));
    }

    public List<Payment> findByOrderId(String orderId) {
        return paymentMapper.findByOrderId(orderId);
    }

    public List<Payment> getAllPayments() {
        return paymentMapper.findAll();
    }

    public Payment updatePayment(String paymentId, Payment paymentDetails) {
        Payment existingPayment = paymentMapper.findByPaymentId(paymentId);
        if (existingPayment == null) {
            throw new RuntimeException("Payment not found with id " + paymentId);
        }
        paymentDetails.setPaymentId(paymentId);
        paymentMapper.update(paymentDetails);
        return paymentDetails;
    }

    public void deletePayment(String paymentId) {
        Payment payment = paymentMapper.findByPaymentId(paymentId);
        if (payment == null) {
            throw new RuntimeException("Payment not found with id " + paymentId);
        }
        paymentMapper.delete(payment);
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

        // 결제 ID를 미리 생성 (주문번호처럼 로그에 일관되게 사용)
        String paymentId = generatePaymentId();
        log.info("Generated payment ID: {}", paymentId);

        // 결제 프로세스 시작: 주문번호는 화면에서 전달된 값 사용
        String orderId = request.getOrderId();
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new RuntimeException("Order ID is required for payment initiation");
        }
        log.info("Using provided order ID: {}", orderId);

        // 인터페이스 로그 먼저 기록 (결제 요청 시작) - 결제ID로 로그 저장
        String requestJson = convertToJson(request);
        
        PaymentInterfaceRequestLog initiateLog = new PaymentInterfaceRequestLog(
                paymentId, // 미리 생성된 결제ID 사용
                "INITIATE_PAYMENT",
                requestJson,
                null // response는 없음
        );
        paymentInterfaceRequestLogMapper.insert(initiateLog);

        // 이니시스 파라미터 구성 - 화면에서 넘어온 값 사용
        InicisPaymentParameters inicisParams = new InicisPaymentParameters();
        inicisParams.setMid(inicisMid);

        // 채번된 주문번호를 OID로 직접 사용
        String oid = orderId;
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

        // 환경설정 기반 콜백 URL (팝업 방식으로 변경)
        inicisParams.setReturnUrl(inicisReturnUrl);
        inicisParams.setCloseUrl(inicisCloseUrl);

        // 인터페이스 로그 업데이트 - 이니시스 파라미터 생성 완료
        String inicisParamsJson = convertToJson(inicisParams);
        
        PaymentInterfaceRequestLog parametersLog = new PaymentInterfaceRequestLog(
                paymentId, // 미리 생성된 결제ID 사용
                "INICIS_PARAMETERS_GENERATED",
                inicisParamsJson,
                null // response는 없음
        );
        paymentInterfaceRequestLogMapper.insert(parametersLog);

        log.info("Payment initiation parameters issued for paymentId={}, order={}, oid={}", paymentId, orderId, oid);
        return inicisParams;
    }

    @Transactional
    public Payment confirmPayment(PaymentConfirmRequest request) {
        log.info("Processing payment confirmation: {}", request);
        log.info("Request details - authToken: {}, authUrl: {}, orderNumber: {}, price: {}", 
                request.getAuthToken(), request.getAuthUrl(), request.getOrderId(), request.getPrice());
        
        // 주문번호 추출 (orderId 필드 사용)
        String orderId = request.getOrderId();

        if (orderId == null || orderId.trim().isEmpty()) {
            throw new RuntimeException("Order ID is required");
        }

        log.info("Extracted order ID: {}", orderId);

        // 결제 ID를 미리 생성 (로그에 일관되게 사용)
        String paymentId = generatePaymentId();
        log.info("Generated payment ID for confirmation: {}", paymentId);

        // 승인 요청 인터페이스 로그 먼저 기록
        String approvalRequestJson = convertToJson(request);
        
        PaymentInterfaceRequestLog approvalRequestLog = new PaymentInterfaceRequestLog(
                paymentId, // 미리 생성된 결제ID 사용
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
                log.info("Starting Inicis approval process for order: {}", orderId);
            try {
                // 실제 이니시스 승인 API 호출
                ApprovalResult result = processInicisApprovalWithResponse(request, paymentId);
                approvalSuccess = result.isSuccess();
                actualResponse = result.getResponseBody();
                
                if (approvalSuccess) {
                    // 이니시스에서 받은 실제 거래 ID 사용
                    transactionId = result.getTransactionId();
                    log.info("Payment approved successfully for order: {}, transactionId: {}", orderId, transactionId);
                } else {
                    log.error("Payment approval failed for order: {}", orderId);
                    // 승인 실패 시 망취소 시도
                    if (request.getNetCancelUrl() != null) {
                        log.info("Attempting net cancel due to approval failure for order: {}", orderId);
                        safeNetCancel(request.getNetCancelUrl(), request.getAuthToken());
                    }
                }
            } catch (Exception e) {
                log.error("Error during Inicis approval process for order: {}", orderId, e);
                // 에러 발생 시 망취소 시도
                if (request.getNetCancelUrl() != null) {
                    log.info("Attempting net cancel due to error for order: {}", orderId);
                    safeNetCancel(request.getNetCancelUrl(), request.getAuthToken());
                }
            }
        }

        // 승인 응답 인터페이스 로그 기록 - 실제 응답값 저장
        String summaryRequestJson = convertToJson(request);
        
        PaymentInterfaceRequestLog approvalResponseLog = new PaymentInterfaceRequestLog(
                paymentId, // 미리 생성된 결제ID 사용
                "INICIS_APPROVAL_RESPONSE",
                summaryRequestJson,
                actualResponse // 실제 이니시스 응답 또는 null
        );
        paymentInterfaceRequestLogMapper.insert(approvalResponseLog);

        // 성공한 경우에만 Payment 테이블에 저장
        Payment payment = null;
        if (approvalSuccess) {
            // request에서 직접 memberId와 paymentMethod 가져오기
            Long memberId = request.getMemberId();
            String paymentMethod = request.getPaymentMethod();
            
            // 필수 필드 검증
            if (memberId == null) {
                throw new RuntimeException("Member ID is required for payment confirmation");
            }
            if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
                throw new RuntimeException("Payment method is required for payment confirmation");
            }
            
            // 포인트 사용 정보 포함하여 Payment 생성
            Double usedPoints = request.getUsedPoints() != null ? request.getUsedPoints() : 0.0;

            payment = new Payment(
                    memberId, // request에서 가져온 memberId 사용
                    orderId, // 주문번호 설정
                    null, // claim_id는 주문 취소/클레임 시에만 사용 (현재는 NULL)
                    Double.valueOf(request.getPrice()),
                    usedPoints, // 사용한 포인트
                    paymentMethod,
                    "INICIS",
                    "SUCCESS",
                    transactionId
            );
            payment.setPaymentId(paymentId); // 미리 생성된 paymentId 사용
            paymentMapper.insert(payment);

            // 포인트 사용 내역 기록 (포인트를 실제로 사용한 경우만)
            if (usedPoints > 0) {
                try {
                    pointHistoryService.recordPointUsage(
                            memberId,
                            usedPoints,
                            paymentId, // payment_id를 reference_id로 사용
                            "결제 시 포인트 사용 - 주문번호: " + orderId
                    );
                    log.info("Point usage recorded: memberId={}, usedPoints={}, paymentId={}",
                            memberId, usedPoints, paymentId);
                } catch (Exception e) {
                    log.error("Failed to record point usage history: memberId={}, usedPoints={}, paymentId={}",
                            memberId, usedPoints, paymentId, e);
                    // 포인트 히스토리 기록 실패해도 결제는 성공으로 처리
                }
            }

            log.info("Payment record created successfully: paymentId={}, memberId={}, order={}, usedPoints={}",
                    payment.getPaymentId(), memberId, orderId, usedPoints);

            // 포인트로만 결제하는 경우 별도 Payment 레코드 생성
            if (usedPoints > 0) {
                createPointPaymentRecord(memberId, orderId, usedPoints, paymentId);
            }
        } else {
            // 실패한 경우 Payment 테이블에는 저장하지 않음
            log.info("Payment failed, no record created in Payment table for order: {}", orderId);
            throw new RuntimeException("Payment approval failed");
        }

        return payment;
    }

    private ApprovalResult processInicisApprovalWithResponse(PaymentConfirmRequest request, String paymentId) {
        String orderNumber = null;
        String requestJson = null;
        String responseJson = null;
        
        try {
            // 주문번호 추출 (로그 저장용)
            orderNumber = request.getOrderId();
            if (orderNumber == null || orderNumber.trim().isEmpty()) {
                log.warn("No order ID provided for logging");
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

            ResponseEntity<InicisApprovalResponse> response = rt.postForEntity(request.getAuthUrl(), new HttpEntity<>(params, headers), InicisApprovalResponse.class);
            InicisApprovalResponse inicisResponse = response.getBody();
            
            // 실제 응답 정보를 JSON으로 저장
            responseJson = convertToJson(response);
            
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
                responseBodyStr = convertToJson(inicisResponse); // ApprovalResult에 넘겨줄 JSON 문자열
                
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
            }
            
            return new ApprovalResult(success, responseBodyStr, tid);
            
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
    public Payment cancelPayment(String paymentId) {
        Payment payment = paymentMapper.findByPaymentId(paymentId);
        if (payment == null) {
            throw new RuntimeException("Payment not found with id " + paymentId);
        }

        if ("CANCELLED".equals(payment.getStatus())) {
            throw new RuntimeException("Payment is already cancelled.");
        }

        // 실제 이니시스 취소 API 호출
        boolean refundSuccess = false;
        String refundResponse = null;
        
        if ("INICIS".equals(payment.getPgCompany()) && payment.getTransactionId() != null) {
            try {
                refundResponse = processInicisRefund(payment.getTransactionId(), paymentId);
                refundSuccess = true;
                log.info("INICIS refund processed successfully for payment: {}, tid: {}", 
                        paymentId, payment.getTransactionId());
            } catch (Exception e) {
                log.error("INICIS refund failed for payment: {}, tid: {}", 
                        paymentId, payment.getTransactionId(), e);
                refundSuccess = false;
                refundResponse = "REFUND_FAILED: " + e.getMessage();
            }
        } else {
            log.warn("No PG refund required for payment: {} (PG: {}, TID: {})", 
                    paymentId, payment.getPgCompany(), payment.getTransactionId());
            refundSuccess = true; // 포인트 결제 등은 PG 취소 불필요
        }
        
        if (!refundSuccess) {
            throw new RuntimeException("PG refund failed for payment: " + paymentId);
        }

        // Update payment status
        payment.setStatus("CANCELLED");
        // transactionId는 그대로 유지 (임의로 변경하지 않음)
        paymentMapper.update(payment);

        // 포인트 복원 처리 (포인트를 실제로 사용한 경우만)
        if (payment.getUsedPoints() != null && payment.getUsedPoints() > 0) {
            try {
                pointHistoryService.recordPointRefund(
                        payment.getMemberId(),
                        payment.getUsedPoints(),
                        paymentId, // payment_id를 reference_id로 사용
                        "결제 취소로 인한 포인트 복원 - 주문번호: " + payment.getOrderId()
                );
                log.info("Point refund recorded: memberId={}, refundPoints={}, paymentId={}",
                        payment.getMemberId(), payment.getUsedPoints(), paymentId);
            } catch (Exception e) {
                log.error("Failed to record point refund history: memberId={}, refundPoints={}, paymentId={}",
                        payment.getMemberId(), payment.getUsedPoints(), paymentId, e);
                // 포인트 히스토리 기록 실패해도 취소는 성공으로 처리
            }
        }

        // Create PaymentInterfaceRequestLog entry for cancellation (실제 객체만 저장)
        PaymentInterfaceRequestLog cancelLog = new PaymentInterfaceRequestLog(
                payment.getPaymentId(),
                "CANCEL_PAYMENT",
                convertToJson(payment), // 실제 payment 객체
                refundResponse // 이니시스 취소 응답
        );
        paymentInterfaceRequestLogMapper.insert(cancelLog);

        // 환불 Payment 레코드 생성
        createRefundPaymentRecord(payment);

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
    
    private static String sha512Hex(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] digest = md.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute SHA-512", e);
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

    // Payment ID 생성 메소드 (17자리 고정 형식)
    private String generatePaymentId() {
        // Payment ID 형식: YYYYMMDDP + 8자리 시퀀스 (총 17자리)
        // 예: 20250918P00000001
        
        // 현재 날짜를 YYYYMMDD 형식으로 가져오기
        java.time.LocalDate today = java.time.LocalDate.now();
        String datePrefix = today.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        // 시퀀스 번호 가져오기 (8자리로 패딩)
        Long sequence = paymentMapper.getNextPaymentSequence();
        String sequenceStr = String.format("%08d", sequence);
        
        // 최종 Payment ID 생성: YYYYMMDDP + 8자리 시퀀스
        return datePrefix + "P" + sequenceStr;
    }
    
    // Claim ID 생성 메소드 (17자리 고정 형식)
    private String generateClaimId() {
        // Claim ID 형식: YYYYMMDDC + 8자리 시퀀스 (총 17자리)
        // 예: 20250918C00000001
        
        // 현재 날짜를 YYYYMMDD 형식으로 가져오기
        java.time.LocalDate today = java.time.LocalDate.now();
        String datePrefix = today.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        // 시퀀스 번호 가져오기 (8자리로 패딩)
        Long sequence = paymentMapper.getNextClaimSequence();
        String sequenceStr = String.format("%08d", sequence);
        
        // 최종 Claim ID 생성: YYYYMMDDC + 8자리 시퀀스
        return datePrefix + "C" + sequenceStr;
    }

    /**
     * 포인트 결제 건을 위한 별도 Payment 레코드 생성
     */
    @Transactional
    private void createPointPaymentRecord(Long memberId, String orderId, Double usedPoints, String relatedPaymentId) {
        try {
            // 포인트 결제용 Payment ID 생성
            String pointPaymentId = generatePaymentId();

            Payment pointPayment = new Payment(
                    memberId,
                    orderId,
                    null, // claim_id
                    usedPoints, // 포인트 사용 금액
                    usedPoints, // 사용된 포인트 (동일)
                    "POINT", // payment_method
                    "PAYMENT", // pay_type
                    null, // pg_company (포인트 결제이므로 null)
                    "SUCCESS", // status
                    relatedPaymentId // transaction_id (관련 결제 ID)
            );

            pointPayment.setPaymentId(pointPaymentId);
            paymentMapper.insert(pointPayment);

            log.info("Point payment record created: paymentId={}, memberId={}, order={}, usedPoints={}",
                    pointPaymentId, memberId, orderId, usedPoints);

        } catch (Exception e) {
            log.error("Failed to create point payment record: memberId={}, orderId={}, usedPoints={}",
                    memberId, orderId, usedPoints, e);
            // 포인트 결제 레코드 생성 실패해도 메인 결제는 성공으로 처리
        }
    }

    /**
     * 환불 시 REFUND payment 레코드 생성
     */
    @Transactional
    private void createRefundPaymentRecord(Payment originalPayment) {
        try {
            // 환불용 Payment ID 생성
            String refundPaymentId = generatePaymentId();

            // 원본 결제건에 대한 환불 레코드 생성 (PG 결제건)
            Payment refundPayment = new Payment(
                    originalPayment.getMemberId(),
                    originalPayment.getOrderId(),
                    null, // claim_id
                    originalPayment.getAmount(), // 환불 금액
                    0.0, // 환불 시에는 포인트 사용 없음
                    originalPayment.getPaymentMethod(),
                    "REFUND", // pay_type
                    originalPayment.getPgCompany(),
                    "SUCCESS", // status
                    originalPayment.getPaymentId() // transaction_id (원본 결제 ID)
            );

            refundPayment.setPaymentId(refundPaymentId);
            paymentMapper.insert(refundPayment);

            log.info("Refund payment record created: paymentId={}, originalPaymentId={}, amount={}",
                    refundPaymentId, originalPayment.getPaymentId(), originalPayment.getAmount());

            // 포인트 환불 레코드도 생성 (포인트를 사용한 경우)
            if (originalPayment.getUsedPoints() != null && originalPayment.getUsedPoints() > 0) {
                String pointRefundPaymentId = generatePaymentId();

                Payment pointRefundPayment = new Payment(
                        originalPayment.getMemberId(),
                        originalPayment.getOrderId(),
                        null, // claim_id
                        originalPayment.getUsedPoints(), // 포인트 환불 금액
                        originalPayment.getUsedPoints(), // 환불되는 포인트
                        "POINT", // payment_method
                        "REFUND", // pay_type
                        null, // pg_company (포인트이므로 null)
                        "SUCCESS", // status
                        originalPayment.getPaymentId() // transaction_id (원본 결제 ID)
                );

                pointRefundPayment.setPaymentId(pointRefundPaymentId);
                paymentMapper.insert(pointRefundPayment);

                log.info("Point refund payment record created: paymentId={}, originalPaymentId={}, usedPoints={}",
                        pointRefundPaymentId, originalPayment.getPaymentId(), originalPayment.getUsedPoints());
            }

        } catch (Exception e) {
            log.error("Failed to create refund payment record: originalPaymentId={}, orderId={}",
                    originalPayment.getPaymentId(), originalPayment.getOrderId(), e);
            // 환불 레코드 생성 실패해도 취소는 성공으로 처리
        }
    }

    /**
     * 이니시스 취소 API 호출
     */
    private String processInicisRefund(String transactionId, String paymentId) {
        try {
            log.info("Starting INICIS refund process for TID: {}, PaymentID: {}", transactionId, paymentId);
            
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
                    transactionId, "취소");
            refundRequest.setData(refundData);
            
            // v2 API 서명 생성: INIAPIKey + " " + mid + " " + type + " " + timestamp + " " + data (SHA512)
            String dataForHash = convertToJson(refundData); // data를 JSON 문자열로 변환
            String signingText = inicisApiKey + inicisMid + refundRequest.getType() + timestamp + dataForHash;
            String hashData = sha512Hex(signingText);
            
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
                    paymentId,
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
                    paymentId,
                    "INICIS_REFUND_RESPONSE",
                    requestJson,
                    responseJson
            );
            paymentInterfaceRequestLogMapper.insert(responseLog);
            
            // 취소 성공 여부 확인
            if (refundResponse != null && "00".equals(refundResponse.getResultCode())) {
                log.info("INICIS refund successful: TID={}, CancelDate={}", 
                        refundResponse.getTid(), refundResponse.getCancelDate());
                return responseJson;
            } else {
                String errorMsg = refundResponse != null ?
                        refundResponse.getResultMsg() : "Unknown error";
                log.error("INICIS refund failed: Code={}, Message={}", 
                        refundResponse != null ? refundResponse.getResultCode() : "NULL", errorMsg);
                throw new RuntimeException("INICIS refund failed: " + errorMsg);
            }
            
        } catch (Exception e) {
            log.error("Error during INICIS refund process for TID: {}", transactionId, e);
            
            // 에러 로그 저장
            PaymentInterfaceRequestLog errorLog = new PaymentInterfaceRequestLog(
                    paymentId,
                    "INICIS_REFUND_ERROR",
                    "TID: " + transactionId,
                    "ERROR: " + e.getMessage()
            );
            paymentInterfaceRequestLogMapper.insert(errorLog);
            
            throw new RuntimeException("INICIS refund API call failed", e);
        }
    }
}