package com.vibe.pay.backend.payment;

import com.vibe.pay.backend.paymentlog.PaymentInterfaceRequestLog;
import com.vibe.pay.backend.paymentlog.PaymentInterfaceRequestLogMapper;
import com.vibe.pay.backend.order.Order;
import com.vibe.pay.backend.order.OrderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Random;
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


@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private PaymentInterfaceRequestLogMapper paymentInterfaceRequestLogMapper;

    @Autowired
    private OrderMapper orderMapper;

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
        log.info("Initiating payment: memberId={}, amount={}, method={}", request.getMemberId(), request.getAmount(), request.getPaymentMethod());

        // PG 선택(샘플)
        Random random = new Random();
        String pgCompany = random.nextBoolean() ? "INICIS" : "NICEPAY";
        log.debug("Selected PG company: {}", pgCompany);

        // 결제 레코드 생성(주문은 결제 성공 후 생성/연결)
        Payment payment = new Payment(
                request.getMemberId(),
                request.getAmount(),
                request.getPaymentMethod(),
                pgCompany,
                "INITIATED",
                null
        );
        paymentMapper.insert(payment);

        // 이니시스 파라미터 구성 - 화면에서 넘어온 값 사용
        InicisPaymentParameters inicisParams = new InicisPaymentParameters();
        inicisParams.setMid(inicisMid);

        // 주문이 아직 없으므로 서버에서 OID 생성(결제ID 기반)
        String oid = "OID-" + payment.getId() + "-" + System.currentTimeMillis();
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
        inicisParams.setMoId(String.valueOf(payment.getId()));
        inicisParams.setGopaymethod(inicisGopaymethod);
        inicisParams.setAcceptmethod(inicisAcceptmethod);

        // 타임스탬프(ms)
        String timestamp = String.valueOf(System.currentTimeMillis());
        inicisParams.setTimestamp(timestamp);

        // signKey 정제
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

        // 인터페이스 로그 적재
        PaymentInterfaceRequestLog logRow = new PaymentInterfaceRequestLog(
                payment.getId(),
                "INITIATE_PAYMENT",
                "Request Payload: memberId=" + request.getMemberId() + ", amount=" + request.getAmount(),
                "Inicis Parameters issued (oid=" + oid + ", price=" + priceStr + ")"
        );
        paymentInterfaceRequestLogMapper.insert(logRow);

        log.info("Payment initiation parameters issued for paymentId={}, oid={}", payment.getId(), oid);
        return inicisParams;
    }

    @Transactional
    public Payment confirmPayment(PaymentConfirmRequest request) {
        log.info("Processing payment confirmation: {}", request);
        log.info("Request details - authToken: {}, authUrl: {}, oid: {}, price: {}", 
                request.getAuthToken(), request.getAuthUrl(), request.getOid(), request.getPrice());
        
        // OID로 결제 정보 찾기 (paymentId가 없는 경우)
        Payment payment = null;
        if (request.getPaymentId() != null) {
            payment = paymentMapper.findById(request.getPaymentId());
        } else {
            // orderNumber 또는 oid로 결제 정보 찾기
            String oidToSearch = request.getOrderNumber() != null ? request.getOrderNumber() : request.getOid();
            if (oidToSearch != null) {
                // OID에서 paymentId 추출 (OID-{paymentId}-{timestamp} 형식)
                try {
                    String[] oidParts = oidToSearch.split("-");
                    if (oidParts.length >= 2) {
                        Long paymentId = Long.parseLong(oidParts[1]);
                        payment = paymentMapper.findById(paymentId);
                    }
                } catch (Exception e) {
                    log.error("Failed to extract paymentId from OID: {}", oidToSearch, e);
                }
            }
        }
        
        if (payment == null) {
            throw new RuntimeException("Payment not found for OID: " + request.getOid());
        }

        // 이니시스 승인 처리
        if (request.getAuthToken() != null && request.getAuthUrl() != null) {
            log.info("Starting Inicis approval process for paymentId: {}", payment.getId());
            try {
                // 실제 이니시스 승인 API 호출
                boolean approvalSuccess = processInicisApproval(request);
                
                if (approvalSuccess) {
                    payment.setStatus("SUCCESS");
                    payment.setTransactionId("TXN-" + System.currentTimeMillis());
                    log.info("Payment approved successfully for paymentId: {}, status: SUCCESS", payment.getId());
                } else {
                    payment.setStatus("FAILED");
                    log.error("Payment approval failed for paymentId: {}, status: FAILED", payment.getId());
                }
            } catch (Exception e) {
                log.error("Error during Inicis approval process for paymentId: {}", payment.getId(), e);
                payment.setStatus("FAILED");
                // 망취소 시도
                if (request.getNetCancelUrl() != null) {
                    log.info("Attempting net cancel for paymentId: {}", payment.getId());
                    safeNetCancel(request.getNetCancelUrl(), request.getAuthToken());
                }
            }
        } else {
            log.info("Using direct status setting for paymentId: {}, status: {}", payment.getId(), request.getStatus());
            // 기존 방식 (직접 상태 설정)
            payment.setStatus(request.getStatus());
            payment.setTransactionId(request.getTransactionId());
        }
        
        paymentMapper.update(payment);

        // 결제 성공 시에만 주문 생성
        if ("SUCCESS".equals(payment.getStatus())) {
            // 주문이 이미 있는지 확인
            Order existingOrder = orderMapper.findByPaymentId(payment.getId());
            if (existingOrder == null) {
                log.info("No existing order found for paymentId: {}, creating new order", payment.getId());
                // 주문 생성은 별도 API에서 처리하도록 함
            } else {
                existingOrder.setStatus("PAID");
                orderMapper.update(existingOrder);
            }
        }

        // 로그 기록
        PaymentInterfaceRequestLog logEntry = new PaymentInterfaceRequestLog(
                payment.getId(),
                "CONFIRM_PAYMENT",
                "Request: " + request.toString(),
                "Response: Status=" + payment.getStatus() + ", TransactionId=" + payment.getTransactionId()
        );
        paymentInterfaceRequestLogMapper.insert(logEntry);

        return payment;
    }
    
    private boolean processInicisApproval(PaymentConfirmRequest request) {
        try {
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

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            RestTemplate rt = new RestTemplate();

            ResponseEntity<String> response = rt.postForEntity(request.getAuthUrl(), new HttpEntity<>(params, headers), String.class);
            String responseBody = response.getBody();
            
            log.info("Inicis approval response: {}", responseBody);
            
            // 응답에서 성공 여부 확인
            return responseBody != null && responseBody.contains("\"resultCode\":\"0000\"");
            
        } catch (Exception e) {
            log.error("Error during Inicis approval process", e);
            return false;
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

        // Create PaymentInterfaceRequestLog entry for cancellation
        PaymentInterfaceRequestLog log = new PaymentInterfaceRequestLog(
                payment.getId(),
                "CANCEL_PAYMENT",
                "Request Payload: Payment ID " + paymentId, // Simplified payload
                "Cancellation Status: SUCCESS, Transaction ID: " + payment.getTransactionId() // Simplified response
        );
        paymentInterfaceRequestLogMapper.insert(log);

        // In a real scenario, you might also update the associated Order status here
        // or handle it in OrderService.cancelOrder
        // For now, OrderService.cancelOrder will handle the order status update.

        return payment;
    }

    private String generateOrdNo() {
        return "ORD-" + System.currentTimeMillis();
    }

        // INIStdPay 리다이렉트 플로우 수신 처리
        public void handleReturn(MultiValueMap<String, String> form) {
            String resultCode = form.getFirst("resultCode");
            String resultMsg = form.getFirst("resultMsg");
            String authToken = form.getFirst("authToken");
            String authUrl = form.getFirst("authUrl");
            String netCancelUrl = form.getFirst("netCancelUrl");
            String mid = form.getFirst("mid");
            String oid = form.getFirst("oid");
            String price = form.getFirst("price");

            log.info("INIStdPay return received: resultCode={}, resultMsg={}, mid={}, oid={}, price={}",
                    resultCode, resultMsg, mid, oid, price);

            if (!"0000".equals(resultCode)) {
                log.error("Payment auth failed at return: {} {}", resultCode, resultMsg);
                throw new RuntimeException("Payment auth failed: " + resultMsg);
            }

            // 승인 요청(approve)
            approveWithAuth(authUrl, netCancelUrl, authToken, mid);
            // TODO: 승인 성공 후 결제/주문 처리(현재 비즈니스 규칙에 맞게 갱신)
        }

        // authUrl로 승인 요청 수행
        private void approveWithAuth(String authUrl, String netCancelUrl, String authToken, String mid) {
            if (authUrl == null || authToken == null) {
                throw new RuntimeException("Missing authUrl/authToken on return");
            }
            String timestamp = String.valueOf(System.currentTimeMillis());
            // 이니시스 승인 서명 규칙 (SHA-256 - 결제 요청과 동일한 방식)
            String signingText = "authToken=" + authToken + "&timestamp=" + timestamp;
            String signature = sha256Hex(signingText);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("mid", (mid != null && !mid.isBlank()) ? mid : inicisMid);
            params.add("authToken", authToken);
            params.add("signature", signature);
            params.add("timestamp", timestamp);
            params.add("charset", "UTF-8");
            params.add("format", "JSON");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            RestTemplate rt = new RestTemplate();

            try {
                ResponseEntity<String> resp = rt.postForEntity(authUrl, new HttpEntity<>(params, headers), String.class);
                String body = resp.getBody();
                log.info("Approval response: {}", body);
                if (body == null || !body.contains("\"resultCode\":\"0000\"")) {
                    // 승인 실패 시 망취소
                    safeNetCancel(netCancelUrl, authToken);
                    throw new RuntimeException("Approval failed");
                }
            } catch (Exception ex) {
                safeNetCancel(netCancelUrl, authToken);
                throw new RuntimeException("Approval error", ex);
            }
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
                log.warn("NetCancel called. Response={}", resp.getBody());
            } catch (Exception ignore) { }
        }

        private static String bytesToHex(byte[] raw) {
            StringBuilder sb = new StringBuilder();
            for (byte b : raw) sb.append(String.format("%02x", b));
            return sb.toString();
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
}