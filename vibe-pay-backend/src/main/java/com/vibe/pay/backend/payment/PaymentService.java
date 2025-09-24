package com.vibe.pay.backend.payment;

import com.vibe.pay.backend.paymentlog.PaymentInterfaceRequestLog;
import com.vibe.pay.backend.paymentlog.PaymentInterfaceRequestLogMapper;
import com.vibe.pay.backend.order.OrderMapper;
import com.vibe.pay.backend.pointhistory.PointHistoryService;
import com.vibe.pay.backend.payment.factory.PaymentProcessorFactory;
import com.vibe.pay.backend.payment.factory.PaymentGatewayFactory;
import com.vibe.pay.backend.payment.processor.PaymentProcessor;
import com.vibe.pay.backend.payment.gateway.PaymentGatewayAdapter;
import com.vibe.pay.backend.payment.gateway.PaymentInitResponse;
import com.vibe.pay.backend.payment.gateway.PaymentConfirmResponse;
import com.vibe.pay.backend.exception.PaymentException;
import com.vibe.pay.backend.enums.PaymentMethod;
import com.vibe.pay.backend.enums.PgCompany;
import com.vibe.pay.backend.common.Constants;
import com.vibe.pay.backend.util.HashUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import java.security.MessageDigest;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
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
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentMapper paymentMapper;
    private final PaymentInterfaceRequestLogMapper paymentInterfaceRequestLogMapper;
    private final OrderMapper orderMapper;
    private final PointHistoryService pointHistoryService;
    private final ObjectMapper objectMapper;
    private final PaymentProcessorFactory paymentProcessorFactory;
    private final PaymentGatewayFactory paymentGatewayFactory;

    @Value("${inicis.mid}")
    private String inicisMid;

    @Value("${inicis.apiKey}")
    private String inicisApiKey;

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
            throw PaymentException.approvalFailed("Payment not found with id " + paymentId);
        }
        paymentDetails.setPaymentId(paymentId);
        paymentMapper.update(paymentDetails);
        return paymentDetails;
    }

    public void deletePayment(String paymentId) {
        Payment payment = paymentMapper.findByPaymentId(paymentId);
        if (payment == null) {
            throw PaymentException.approvalFailed("Payment not found with id " + paymentId);
        }
        paymentMapper.delete(payment);
    }

    @Transactional
    public PaymentInitResponse initiatePayment(PaymentInitiateRequest request) {
        log.info("Initiating payment with Factory/Adapter pattern: orderId={}, method={}, pgCompany={}",
                request.getOrderId(), request.getPaymentMethod(), "INICIS");

        try {
            // 1. PG 어댑터 선택 (현재는 INICIS만 지원, 추후 확장 가능)
            PaymentGatewayAdapter pgAdapter = paymentGatewayFactory.getAdapter(PgCompany.INICIS.getCode());
            
            // 2. PG 결제 시작 처리
            PaymentInitResponse pgResponse = pgAdapter.initiate(request);
            
            if (!pgResponse.isSuccess()) {
                throw PaymentException.initiationFailed(pgResponse.getErrorMessage());
            }

            log.info("Payment initiation completed: orderId={}, paymentId={}", 
                    request.getOrderId(), pgResponse.getPaymentId());
            
            return pgResponse;

        } catch (PaymentException e) {
            log.error("Payment initiation failed: {}", e.getErrorMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during payment initiation: {}", e.getMessage(), e);
            throw PaymentException.pgSystemError("PAYMENT_SYSTEM", e);
        }
    }

    @Transactional
    public Payment confirmPayment(PaymentConfirmRequest request) {
        log.info("Processing payment confirmation with Factory/Adapter pattern: orderId={}, method={}",
                request.getOrderId(), request.getPaymentMethod());

        try {
            // 1. 결제수단별 Processor 선택
            PaymentProcessor processor = paymentProcessorFactory.getProcessor(request.getPaymentMethod());

            // 2. 카드 결제인 경우 PG Adapter 사용하여 승인 처리
            if (PaymentMethod.CREDIT_CARD.getCode().equals(request.getPaymentMethod())) {
                PaymentGatewayAdapter pgAdapter = paymentGatewayFactory.getAdapter(PgCompany.INICIS.getCode());
                
                // PG 승인 처리
                PaymentConfirmResponse pgResponse = pgAdapter.confirm(request);
                if (!pgResponse.isSuccess()) {
                    // 승인 실패 시 망취소 시도
                    if (request.getNetCancelUrl() != null && request.getAuthToken() != null) {
                        try {
                            performNetCancel(request.getNetCancelUrl(), request.getAuthToken(), request.getOrderId());
                        } catch (Exception netCancelException) {
                            log.error("Net cancel failed after approval failure: {}", netCancelException.getMessage());
                        }
                    }
                    throw PaymentException.approvalFailed(pgResponse.getErrorMessage());
                }

                // 승인 정보를 request에 반영
                request.setAuthToken(pgResponse.getTransactionId());
            }

            // 3. 결제 처리 (카드/포인트 공통)
            Payment payment = processor.processPayment(request);

            // 4. 포인트 사용 내역 기록 (포인트를 실제로 사용한 경우만)
            Double usedPoints = request.getUsedPoints() != null ? request.getUsedPoints() : 0.0;
            if (usedPoints > 0) {
                try {
                    pointHistoryService.recordPointUsage(
                            request.getMemberId(),
                            usedPoints,
                            payment.getPaymentId(),
                            "결제 시 포인트 사용 - 주문번호: " + request.getOrderId()
                    );
                    log.info("Point usage recorded: memberId={}, usedPoints={}, paymentId={}",
                            request.getMemberId(), usedPoints, payment.getPaymentId());

                    // 포인트 결제 별도 Payment 레코드 생성
                    createPointPaymentRecord(request.getMemberId(), request.getOrderId(), usedPoints, payment.getPaymentId());
                } catch (Exception e) {
                    log.error("Failed to record point usage: {}", e.getMessage(), e);
                    // 포인트 히스토리 기록 실패해도 결제는 성공으로 처리
                }
            }

            log.info("Payment processed successfully: paymentId={}, method={}, amount={}",
                    payment.getPaymentId(), payment.getPaymentMethod(), payment.getAmount());

            return payment;

        } catch (PaymentException e) {
            log.error("Payment confirmation failed: {}", e.getErrorMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during payment confirmation: {}", e.getMessage(), e);
            throw PaymentException.pgSystemError("PAYMENT_SYSTEM", e);
        }
    }


    



    @Transactional
    public Payment cancelPayment(String paymentId) {
        log.info("Processing payment cancellation with Factory/Adapter pattern: paymentId={}", paymentId);

        try {
            // 1. 원본 결제 조회
            Payment originalPayment = paymentMapper.findByPaymentId(paymentId);
            if (originalPayment == null) {
                throw PaymentException.approvalFailed("Payment not found: " + paymentId);
            }

            if ("FAIL".equals(originalPayment.getStatus())) {
                throw PaymentException.approvalFailed("Payment is already cancelled: " + paymentId);
            }

            // 2. 결제수단별 Processor 선택
            PaymentProcessor processor = paymentProcessorFactory.getProcessor(originalPayment.getPaymentMethod());

            // 3. 취소 처리
            Payment cancelPayment = processor.processRefund(originalPayment);

            // 4. 포인트 결제인 경우 포인트 환불 처리
            if ("POINT".equals(originalPayment.getPaymentMethod())) {
                try {
                    pointHistoryService.recordPointRefund(
                            originalPayment.getMemberId(),
                            originalPayment.getAmount(),
                            originalPayment.getPaymentId(),
                            "주문 취소로 인한 포인트 환불 - 주문번호: " + originalPayment.getOrderId()
                    );
                    log.info("Point refund recorded: memberId={}, refundAmount={}, paymentId={}",
                            originalPayment.getMemberId(), originalPayment.getAmount(), originalPayment.getPaymentId());
                } catch (Exception e) {
                    log.error("Failed to record point refund: {}", e.getMessage(), e);
                    // 포인트 히스토리 기록 실패해도 취소는 성공으로 처리
                }
            }

            // 5. 환불 Payment 레코드 생성 (claim_id는 나중에 OrderService에서 설정)
            createRefundPaymentRecord(originalPayment, null);

            log.info("Payment cancelled successfully: originalPaymentId={}, cancelPaymentId={}",
                    paymentId, cancelPayment.getPaymentId());

            return cancelPayment;

        } catch (PaymentException e) {
            log.error("Payment cancellation failed: {}", e.getErrorMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during payment cancellation: {}", e.getMessage(), e);
            throw PaymentException.pgSystemError("PAYMENT_SYSTEM", e);
        }
    }




        /**
         * 외부에서 호출 가능한 망취소 메서드 (주문 생성 실패 시 사용)
         * @param netCancelUrl 이니시스 망취소 URL
         * @param authToken 결제 승인 토큰
         * @param orderNumber 주문번호 (로그용)
         */
        public void performNetCancel(String netCancelUrl, String authToken, String orderNumber) {
            log.info("Performing net cancel for order: {}, netCancelUrl: {}", orderNumber, netCancelUrl);
            
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
                
                // 해당 주문의 원본 결제 ID 찾기
                String originalPaymentId = null;
                try {
                    List<Payment> payments = findByOrderId(orderNumber);
                    if (!payments.isEmpty()) {
                        // 첫 번째 결제건의 ID 사용 (카드 결제 우선)
                        originalPaymentId = payments.stream()
                                .filter(p -> !"POINT".equals(p.getPaymentMethod()))
                                .findFirst()
                                .orElse(payments.get(0))
                                .getPaymentId();
                    }
                } catch (Exception e) {
                    log.warn("Failed to find original payment ID for order: {}", orderNumber);
                }
                
                PaymentInterfaceRequestLog netCancelLog = new PaymentInterfaceRequestLog(
                        originalPaymentId, // 원본 결제 ID 사용
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
                    // 해당 주문의 원본 결제 ID 찾기
                    String originalPaymentId = null;
                    try {
                        List<Payment> payments = findByOrderId(orderNumber);
                        if (!payments.isEmpty()) {
                            originalPaymentId = payments.stream()
                                    .filter(p -> !"POINT".equals(p.getPaymentMethod()))
                                    .findFirst()
                                    .orElse(payments.get(0))
                                    .getPaymentId();
                        }
                    } catch (Exception findException) {
                        log.warn("Failed to find original payment ID for error log: {}", orderNumber);
                    }
                    
                    PaymentInterfaceRequestLog errorLog = new PaymentInterfaceRequestLog(
                            originalPaymentId, // 원본 결제 ID 사용
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
        String datePrefix = today.format(Constants.DATE_FORMATTER_YYYYMMDD);
        
        // 시퀀스 번호 가져오기 (8자리로 패딩)
        Long sequence = paymentMapper.getNextPaymentSequence();
        String sequenceStr = String.format("%0" + Constants.SEQUENCE_PADDING_LENGTH + "d", sequence);
        
        // 최종 Payment ID 생성: YYYYMMDDP + 8자리 시퀀스
        return datePrefix + Constants.PAYMENT_ID_PREFIX + sequenceStr;
    }
    
    // Claim ID 생성 메소드 (17자리 고정 형식)
    private String generateClaimId() {
        // Claim ID 형식: YYYYMMDDC + 8자리 시퀀스 (총 17자리)
        // 예: 20250918C00000001
        
        // 현재 날짜를 YYYYMMDD 형식으로 가져오기
        java.time.LocalDate today = java.time.LocalDate.now();
        String datePrefix = today.format(Constants.DATE_FORMATTER_YYYYMMDD);
        
        // 시퀀스 번호 가져오기 (8자리로 패딩)
        Long sequence = paymentMapper.getNextClaimSequence();
        String sequenceStr = String.format("%0" + Constants.SEQUENCE_PADDING_LENGTH + "d", sequence);
        
        // 최종 Claim ID 생성: YYYYMMDDC + 8자리 시퀀스
        return datePrefix + Constants.CLAIM_ID_PREFIX + sequenceStr;
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
                    "POINT", // payment_method
                    "PAYMENT", // pay_type
                    null, // pg_company (포인트 결제이므로 null)
                    "SUCCESS", // status
                    relatedPaymentId // transaction_id (관련 결제 ID)
            );

            pointPayment.setPaymentId(pointPaymentId);
            pointPayment.setOrderStatus("ORDER"); // 주문시 ORDER
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
    private void createRefundPaymentRecord(Payment originalPayment, String claimId) {
        try {
            // 환불용 Payment ID 생성
            String refundPaymentId = generatePaymentId();

            // 원본 결제건에 대한 환불 레코드 생성 (PG 결제건)
            Payment refundPayment = new Payment(
                    originalPayment.getMemberId(),
                    originalPayment.getOrderId(),
                    claimId, // 주문 취소시 생성된 claim_id
                    originalPayment.getAmount(), // 환불 금액
                    originalPayment.getPaymentMethod(), // payment_method (원본과 동일)
                    "REFUND", // pay_type
                    originalPayment.getPgCompany(),
                    "SUCCESS", // status
                    originalPayment.getPaymentId() // transaction_id (원본 결제 ID)
            );

            refundPayment.setPaymentId(refundPaymentId);
            refundPayment.setOrderStatus("CANCELED"); // 주문 취소로 인한 환불
            paymentMapper.insert(refundPayment);

            log.info("Refund payment record created: paymentId={}, originalPaymentId={}, amount={}",
                    refundPaymentId, originalPayment.getPaymentId(), originalPayment.getAmount());

            // 포인트 환불 레코드는 별도의 POINT 결제 취소에서 처리됨
            // (포인트 사용 시 별도의 POINT payment 레코드가 생성되므로 해당 레코드의 취소에서 처리)

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