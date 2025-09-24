package com.vibe.pay.backend.payment.gateway;

import com.vibe.pay.backend.enums.PgCompany;
import com.vibe.pay.backend.payment.PaymentInitiateRequest;
import com.vibe.pay.backend.payment.PaymentConfirmRequest;
import com.vibe.pay.backend.util.HashUtils;
import com.vibe.pay.backend.util.WebClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.vibe.pay.backend.common.Constants;

@Component
public class InicisAdapter implements PaymentGatewayAdapter {

    private static final Logger log = LoggerFactory.getLogger(InicisAdapter.class);

    @Autowired
    private WebClientUtil webClientUtil;

    @Value("${inicis.mid}")
    private String mid;

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

            response.setMid(mid);
            response.setOid(request.getOrderId());
            response.setPrice(request.getAmount().longValue());
            response.setGoodName(goodName);
            response.setMoId(mid);
            
            // Standard fields (for internal use)
            response.setPaymentId(generatePaymentId());
            response.setMerchantId(mid);
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
        try {
            log.info("Confirming INICIS payment for order: {}", request.getOrderId());

            // 이니시스 결제 승인 API 호출
            MultiValueMap<String, String> confirmParams = new LinkedMultiValueMap<>();
            confirmParams.add("authToken", request.getAuthToken());
            confirmParams.add("mid", mid);

            // WebClient를 사용하여 이니시스 승인 API 호출
            String response = webClientUtil.postForm(
                request.getAuthUrl(),
                confirmParams,
                String.class
            );

            // 응답 파싱 (실제로는 JSON이나 XML 파싱이 필요)
            boolean isSuccess = response.contains("PAID");
            String transactionId = extractTransactionId(response);

            return new PaymentConfirmResponse(isSuccess, transactionId, request.getPrice().toString(), "SUCCESS");

        } catch (Exception e) {
            log.error("Failed to confirm INICIS payment: {}", e.getMessage(), e);
            PaymentConfirmResponse errorResponse = new PaymentConfirmResponse();
            errorResponse.setSuccess(false);
            errorResponse.setErrorMessage("결제 승인 실패: " + e.getMessage());
            return errorResponse;
        }
    }

    @Override
    public PaymentCancelResponse cancel(PaymentCancelRequest request) {
        try {
            log.info("Cancelling INICIS payment for transaction: {}", request.getTransactionId());

            MultiValueMap<String, String> cancelParams = new LinkedMultiValueMap<>();
            cancelParams.add("mid", mid);
            cancelParams.add("tid", request.getTransactionId());
            cancelParams.add("cancelAmount", request.getAmount());
            cancelParams.add("cancelReason", request.getReason());

            // 취소 API 호출
            String response = webClientUtil.postForm(
                "https://stgstdpay.inicis.com/stdpay/cancel.ini",
                cancelParams,
                String.class
            );

            boolean isSuccess = response.contains("CANCELLED");
            return new PaymentCancelResponse(isSuccess, request.getTransactionId(), request.getAmount(), "CANCELLED");

        } catch (Exception e) {
            log.error("Failed to cancel INICIS payment: {}", e.getMessage(), e);
            PaymentCancelResponse errorResponse = new PaymentCancelResponse();
            errorResponse.setSuccess(false);
            errorResponse.setErrorMessage("결제 취소 실패: " + e.getMessage());
            return errorResponse;
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


    private String extractTransactionId(String response) {
        // 실제로는 응답에서 거래ID를 파싱해야 함
        return "INICIS_TXN_" + System.currentTimeMillis();
    }

    private String generatePaymentId() {
        String dateStr = LocalDateTime.now().format(Constants.DATE_FORMATTER_YYYYMMDD);
        long sequence = System.currentTimeMillis() % 100000000L;
        return dateStr + "P" + String.format("%08d", sequence);
    }
}