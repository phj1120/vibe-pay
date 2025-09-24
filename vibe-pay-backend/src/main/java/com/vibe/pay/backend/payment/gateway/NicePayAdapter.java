package com.vibe.pay.backend.payment.gateway;

import com.vibe.pay.backend.enums.PgCompany;
import com.vibe.pay.backend.payment.PaymentInitiateRequest;
import com.vibe.pay.backend.payment.PaymentConfirmRequest;
import com.vibe.pay.backend.util.WebClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.vibe.pay.backend.common.Constants;
import java.util.HashMap;
import java.util.Map;

@Component
public class NicePayAdapter implements PaymentGatewayAdapter {

    private static final Logger log = LoggerFactory.getLogger(NicePayAdapter.class);

    @Autowired
    private WebClientUtil webClientUtil;

    @Value("${nicepay.mid:}")
    private String mid;

    @Value("${nicepay.merchantKey:}")
    private String merchantKey;

    @Value("${nicepay.returnUrl:}")
    private String returnUrl;

    @Value("${nicepay.cancelUrl:}")
    private String cancelUrl;

    @Override
    public PaymentInitResponse initiate(PaymentInitiateRequest request) {
        try {
            log.info("Initiating NicePay payment for order: {}", request.getOrderId());

            // 나이스페이 결제 파라미터 생성
            String ediDate = LocalDateTime.now().format(Constants.DATETIME_FORMATTER_YYYYMMDDHHMMSS);

            Map<String, Object> params = new HashMap<>();
            params.put("PayMethod", "CARD");
            params.put("MID", mid);
            params.put("Moid", request.getOrderId());
            params.put("Amt", request.getAmount());
            params.put("BuyerName", request.getBuyerName() != null ? request.getBuyerName() : "구매자");
            params.put("BuyerEmail", request.getBuyerEmail() != null ? request.getBuyerEmail() : "");
            params.put("BuyerTel", request.getBuyerTel() != null ? request.getBuyerTel() : "");
            params.put("ReturnURL", returnUrl);
            params.put("VbankExpDate", "");
            params.put("EdiDate", ediDate);

            // 나이스페이는 클라이언트 사이드에서 직접 호출하는 구조
            // PaymentInitResponse를 모든 필드와 함께 구성
            PaymentInitResponse response = new PaymentInitResponse();
            response.setSuccess(true);
            response.setPaymentUrl("https://web.nicepay.co.kr/v3/v3Payment.jsp");
            response.setPaymentParams(params.toString());
            
            // NicePay 특화 필드들 설정
            response.setPaymentId(generatePaymentId());
            response.setMerchantId("nicepay_merchant_id");
            response.setOrderId(request.getOrderId());
            response.setAmount(request.getAmount().toString());
            response.setProductName(request.getGoodName() != null ? request.getGoodName() : "VibePay 주문");
            response.setBuyerName(request.getBuyerName() != null ? request.getBuyerName() : "구매자");
            response.setBuyerTel(request.getBuyerTel() != null ? request.getBuyerTel() : "010-0000-0000");
            response.setBuyerEmail(request.getBuyerEmail() != null ? request.getBuyerEmail() : "buyer@example.com");
            response.setTimestamp(ediDate);
            response.setMKey("nicepay_mkey");
            response.setSignature("nicepay_signature");
            response.setVerification("nicepay_verification");
            response.setReturnUrl("http://localhost:3000/payment/return");
            response.setCloseUrl("http://localhost:3000/payment/close");
            response.setVersion("1.0");
            response.setCurrency("KRW");
            response.setGopaymethod("Card");
            response.setAcceptmethod("Card");
            
            return response;

        } catch (Exception e) {
            log.error("Failed to initiate NicePay payment: {}", e.getMessage(), e);
            PaymentInitResponse errorResponse = new PaymentInitResponse();
            errorResponse.setSuccess(false);
            errorResponse.setErrorMessage("나이스페이 결제 초기화 실패: " + e.getMessage());
            return errorResponse;
        }
    }

    @Override
    public PaymentConfirmResponse confirm(PaymentConfirmRequest request) {
        try {
            log.info("Confirming NicePay payment for order: {}", request.getOrderId());

            Map<String, Object> confirmParams = new HashMap<>();
            confirmParams.put("MID", mid);
            confirmParams.put("TID", request.getAuthToken()); // 나이스페이의 경우 TID
            confirmParams.put("Amt", request.getPrice());

            // 나이스페이 승인 API 호출
            Map<String, Object> response = webClientUtil.postJson(
                "https://webapi.nicepay.co.kr/webapi/pay_process.jsp",
                confirmParams,
                Map.class
            );

            boolean isSuccess = "0000".equals(response.get("ResultCode"));
            String transactionId = (String) response.get("TID");

            return new PaymentConfirmResponse(isSuccess, transactionId, request.getPrice(), "SUCCESS");

        } catch (Exception e) {
            log.error("Failed to confirm NicePay payment: {}", e.getMessage(), e);
            PaymentConfirmResponse errorResponse = new PaymentConfirmResponse();
            errorResponse.setSuccess(false);
            errorResponse.setErrorMessage("나이스페이 결제 승인 실패: " + e.getMessage());
            return errorResponse;
        }
    }

    @Override
    public void cancel(PaymentCancelRequest request) {
        try {
            log.info("Cancelling NicePay payment for transaction: {}", request.getTransactionId());

            Map<String, Object> cancelParams = new HashMap<>();
            cancelParams.put("MID", mid);
            cancelParams.put("TID", request.getTransactionId());
            cancelParams.put("CancelAmt", request.getAmount());
            cancelParams.put("PartialCancelCode", "0"); // 전체 취소
            cancelParams.put("CancelMsg", request.getReason());

            // 취소 API 호출
            Map<String, Object> response = webClientUtil.postJson(
                "https://webapi.nicepay.co.kr/webapi/cancel_process.jsp",
                cancelParams,
                Map.class
            );

            boolean isSuccess = "2001".equals(response.get("ResultCode")); // 나이스페이 취소 성공 코드

        } catch (Exception e) {
            log.error("Failed to cancel NicePay payment: {}", e.getMessage(), e);
            PaymentCancelResponse errorResponse = new PaymentCancelResponse();
            errorResponse.setSuccess(false);
            errorResponse.setErrorMessage("나이스페이 결제 취소 실패: " + e.getMessage());
        }
    }

    @Override
    public void netCancel(PaymentNetCancelRequest request) {

    }

    @Override
    public boolean supports(String pgCompany) {
        return PgCompany.NICEPAY.getCode().equals(pgCompany);
    }

    private String generatePaymentId() {
        String dateStr = LocalDateTime.now().format(Constants.DATE_FORMATTER_YYYYMMDD);
        long sequence = System.currentTimeMillis() % 100000000L;
        return dateStr + "P" + String.format("%08d", sequence);
    }
}