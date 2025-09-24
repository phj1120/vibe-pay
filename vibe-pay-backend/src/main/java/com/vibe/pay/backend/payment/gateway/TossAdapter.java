package com.vibe.pay.backend.payment.gateway;

import com.vibe.pay.backend.enums.PgCompany;
import com.vibe.pay.backend.payment.PaymentInitiateRequest;
import com.vibe.pay.backend.payment.PaymentConfirmRequest;
import com.vibe.pay.backend.payment.dto.*;
import com.vibe.pay.backend.util.WebClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import com.vibe.pay.backend.common.Constants;

@Component
public class TossAdapter implements PaymentGatewayAdapter {

    private static final Logger log = LoggerFactory.getLogger(TossAdapter.class);

    @Autowired
    private WebClientUtil webClientUtil;

    @Value("${toss.clientKey:}")
    private String clientKey;

    @Value("${toss.secretKey:}")
    private String secretKey;

    @Value("${toss.successUrl:}")
    private String successUrl;

    @Value("${toss.failUrl:}")
    private String failUrl;

    @Override
    public PaymentInitResponse initiate(PaymentInitiateRequest request) {
        try {
            log.info("Initiating Toss payment for order: {}", request.getOrderId());

            // 토스페이먼츠는 클라이언트에서 직접 SDK를 사용하는 구조
            Map<String, Object> params = new HashMap<>();
            params.put("clientKey", clientKey);
            params.put("amount", request.getAmount());
            params.put("orderId", request.getOrderId());
            params.put("orderName", "VibePay 주문");
            params.put("successUrl", successUrl);
            params.put("failUrl", failUrl);

            if (request.getBuyerName() != null) {
                params.put("customerName", request.getBuyerName());
            }
            if (request.getBuyerEmail() != null) {
                params.put("customerEmail", request.getBuyerEmail());
            }

            // PaymentInitResponse를 모든 필드와 함께 구성
            PaymentInitResponse response = new PaymentInitResponse();
            response.setSuccess(true);
            response.setPaymentUrl("https://js.tosspayments.com/v1/payment");
            response.setPaymentParams(params.toString());
            
            // Toss 특화 필드들 설정
            response.setPaymentId(generatePaymentId());
            response.setMerchantId("toss_merchant_id");
            response.setOrderId(request.getOrderId());
            response.setAmount(request.getAmount().toString());
            response.setProductName(request.getGoodName() != null ? request.getGoodName() : "VibePay 주문");
            response.setBuyerName(request.getBuyerName() != null ? request.getBuyerName() : "구매자");
            response.setBuyerTel(request.getBuyerTel() != null ? request.getBuyerTel() : "010-0000-0000");
            response.setBuyerEmail(request.getBuyerEmail() != null ? request.getBuyerEmail() : "buyer@example.com");
            String timestamp = LocalDateTime.now().format(Constants.DATETIME_FORMATTER_YYYYMMDDHHMMSS);
            response.setTimestamp(timestamp);
            response.setMKey("toss_mkey");
            response.setSignature("toss_signature");
            response.setVerification("toss_verification");
            response.setReturnUrl("http://localhost:3000/payment/return");
            response.setCloseUrl("http://localhost:3000/payment/close");
            response.setVersion("1.0");
            response.setCurrency("KRW");
            response.setGopaymethod("Card");
            response.setAcceptmethod("Card");
            
            return response;

        } catch (Exception e) {
            log.error("Failed to initiate Toss payment: {}", e.getMessage(), e);
            PaymentInitResponse errorResponse = new PaymentInitResponse();
            errorResponse.setSuccess(false);
            errorResponse.setErrorMessage("토스페이먼츠 결제 초기화 실패: " + e.getMessage());
            return errorResponse;
        }
    }

    @Override
    public PaymentConfirmResponse confirm(PaymentConfirmRequest request) {
        try {
            log.info("Confirming Toss payment for order: {}", request.getOrderId());

            // 토스페이먼츠 승인 API 호출
            Map<String, Object> confirmParams = new HashMap<>();
            confirmParams.put("paymentKey", request.getAuthToken()); // 토스의 경우 paymentKey
            confirmParams.put("orderId", request.getOrderId());
            confirmParams.put("amount", request.getPrice());

            // Basic Auth 헤더 생성
            Map<String, String> headers = new HashMap<>();
            String credentials = secretKey + ":";
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
            headers.put("Authorization", "Basic " + encodedCredentials);

            // 토스페이먼츠 승인 API 호출
            Map<String, Object> response = webClientUtil.postWithHeaders(
                "https://api.tosspayments.com/v1/payments/confirm",
                confirmParams,
                headers,
                Map.class
            );

            boolean isSuccess = "DONE".equals(response.get("status"));
            String transactionId = (String) response.get("paymentKey");

            return new PaymentConfirmResponse(isSuccess, transactionId, request.getPrice(), "SUCCESS");

        } catch (Exception e) {
            log.error("Failed to confirm Toss payment: {}", e.getMessage(), e);
            PaymentConfirmResponse errorResponse = new PaymentConfirmResponse();
            errorResponse.setSuccess(false);
            errorResponse.setErrorMessage("토스페이먼츠 결제 승인 실패: " + e.getMessage());
            return errorResponse;
        }
    }

    @Override
    public void cancel(PaymentCancelRequest request) {
        try {
            log.info("Cancelling Toss payment for transaction: {}", request.getTransactionId());

            Map<String, Object> cancelParams = new HashMap<>();
            cancelParams.put("cancelReason", request.getReason());
            if (request.getAmount() != null) {
                cancelParams.put("cancelAmount", request.getAmount());
            }

            // Basic Auth 헤더 생성
            Map<String, String> headers = new HashMap<>();
            String credentials = secretKey + ":";
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
            headers.put("Authorization", "Basic " + encodedCredentials);

            // 취소 API 호출
            String cancelUrl = "https://api.tosspayments.com/v1/payments/" + request.getTransactionId() + "/cancel";
            Map<String, Object> response = webClientUtil.postWithHeaders(
                cancelUrl,
                cancelParams,
                headers,
                Map.class
            );

            boolean isSuccess = "CANCELED".equals(response.get("status"));

        } catch (Exception e) {
            log.error("Failed to cancel Toss payment: {}", e.getMessage(), e);
            PaymentCancelResponse errorResponse = new PaymentCancelResponse();
            errorResponse.setSuccess(false);
            errorResponse.setErrorMessage("토스페이먼츠 결제 취소 실패: " + e.getMessage());
        }
    }

    @Override
    public void netCancel(PaymentNetCancelRequest request) {

    }

    @Override
    public boolean supports(String pgCompany) {
        return PgCompany.TOSS.getCode().equals(pgCompany);
    }

    private String generatePaymentId() {
        String dateStr = LocalDateTime.now().format(Constants.DATE_FORMATTER_YYYYMMDD);
        long sequence = System.currentTimeMillis() % 100000000L;
        return dateStr + "P" + String.format("%08d", sequence);
    }
}