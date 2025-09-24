package com.vibe.pay.backend.payment.gateway;

import com.vibe.pay.backend.enums.PgCompany;
import com.vibe.pay.backend.payment.PaymentInitiateRequest;
import com.vibe.pay.backend.payment.PaymentConfirmRequest;
import com.vibe.pay.backend.payment.dto.*;
import com.vibe.pay.backend.util.WebClientUtil;
import com.vibe.pay.backend.paymentlog.PaymentInterfaceRequestLog;
import com.vibe.pay.backend.paymentlog.PaymentInterfaceRequestLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

@Component
public class NicePayAdapter implements PaymentGatewayAdapter {

    private static final Logger log = LoggerFactory.getLogger(NicePayAdapter.class);

    @Autowired
    private WebClientUtil webClientUtil;

    @Autowired
    private PaymentInterfaceRequestLogMapper paymentInterfaceRequestLogMapper;

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
            String ediDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

            // SignData 생성을 위한 파라미터
            String payMethod = "CARD";
            String moid = request.getOrderId();
            String amt = request.getAmount().toString();
            String buyerName = request.getBuyerName() != null ? request.getBuyerName() : "구매자";
            String buyerEmail = request.getBuyerEmail() != null ? request.getBuyerEmail() : "";
            String buyerTel = request.getBuyerTel() != null ? request.getBuyerTel() : "";

            // SignData 생성 (SHA256)
            String signData = generateSignData(mid, amt, ediDate, merchantKey);

            // PaymentInitResponse 구성 (이니시스와 공통 구조 사용)
            PaymentInitResponse response = new PaymentInitResponse();
            response.setSuccess(true);
            response.setPaymentUrl("https://web.nicepay.co.kr/v3/v3Payment.jsp");

            // 공통 필드들 설정
            response.setPaymentId(generatePaymentId());
            response.setMid(mid);
            response.setMoId(moid);
            response.setAmt(request.getAmount()); // NicePay는 Long 타입 그대로 사용
            response.setGoodName(request.getGoodName() != null ? request.getGoodName() : "VibePay 주문");
            response.setBuyerName(buyerName);
            response.setBuyerTel(buyerTel);
            response.setBuyerEmail(buyerEmail);
            response.setReturnUrl(returnUrl);
            response.setCloseUrl(cancelUrl);

            // NicePay 특화 필드들
            response.setEdiDate(ediDate);
            response.setSignData(signData);
            response.setVersion("1.0");
            response.setCurrency("KRW");
            
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

            // 나이스페이 승인 API 파라미터 구성
            String tid = request.getTxTid(); // TID는 인증 응답의 TxTid 사용
            String authToken = request.getAuthToken(); // AuthToken
            String amt = request.getPrice().toString();
            String ediDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

            // SignData 생성: hex(sha256(AuthToken + MID + Amt + EdiDate + MerchantKey))
            String signData = generateConfirmSignData(authToken, mid, amt, ediDate, merchantKey);

            // NicePayConfirmRequest DTO 생성
            NicePayConfirmRequest confirmRequest = new NicePayConfirmRequest();
            confirmRequest.setTID(tid);                    // 30 byte 필수 거래번호
            confirmRequest.setAuthToken(authToken);        // 40 byte 필수 인증 TOKEN
            confirmRequest.setMID(mid);                    // 10 byte 필수 가맹점아이디
            confirmRequest.setAmt(amt);                    // 12 byte 필수 금액
            confirmRequest.setEdiDate(ediDate);            // 14 byte 필수 전문생성일시
            confirmRequest.setSignData(signData);          // 256 byte 필수 서명데이터

            log.info("NicePay confirm request: {}", confirmRequest);

            // 나이스페이 승인 API 호출 (NextAppURL 사용)
            String approvalUrl = request.getNextAppUrl();
            log.info("NicePay approval URL: {}", approvalUrl);

            NicePayConfirmResponse response = webClientUtil.postDtoForDto(
                approvalUrl,
                confirmRequest,
                NicePayConfirmResponse.class
            );

            log.info("NicePay confirm response: {}", response);

            // Signature 검증 (TID + MID + Amt + MerchantKey)
            String expectedSignature = generateResponseSignature(response.getTID(), response.getMID(), response.getAmt(), merchantKey);
            if (!expectedSignature.equals(response.getSignature())) {
                log.error("NicePay signature verification failed. Expected: {}, Actual: {}", expectedSignature, response.getSignature());
                throw new RuntimeException("나이스페이 응답 서명 검증 실패");
            }

            // 응답 결과 확인 (3001: 신용카드 성공)
            boolean isSuccess = "3001".equals(response.getResultCode());
            String transactionId = response.getTID();
            String resultCode = response.getResultCode();

            return new PaymentConfirmResponse(isSuccess, transactionId, request.getPrice(), resultCode);
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
        PaymentInterfaceRequestLog requestLog = null;
        try {
            log.info("Cancelling NicePay payment for transaction: {}", request.getTransactionId());

            // 나이스페이 취소 API 파라미터 구성
            String ediDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

            Map<String, Object> cancelParams = new HashMap<>();
            cancelParams.put("MID", mid);
            cancelParams.put("TID", request.getTransactionId());
            cancelParams.put("CancelAmt", request.getAmount().toString());
            cancelParams.put("PartialCancelCode", "0"); // 전체 취소
            cancelParams.put("CancelMsg", request.getReason() != null ? request.getReason() : "결제 취소");
            cancelParams.put("EdiDate", ediDate);

            // SignData 생성: hex(sha256(MID + CancelAmt + EdiDate + MerchantKey))
            String signData = generateCancelSignData(mid, request.getAmount().toString(), ediDate, merchantKey);
            cancelParams.put("SignData", signData);

            // 요청 로그 기록
            requestLog = new PaymentInterfaceRequestLog();
            requestLog.setPaymentId(request.getOrderId());
            requestLog.setRequestType("NICEPAY_CANCEL");
            requestLog.setRequestPayload(cancelParams.toString());
            requestLog.setTimestamp(LocalDateTime.now());

            // 취소 API 호출
            Map<String, Object> response = webClientUtil.postJson(
                "https://webapi.nicepay.co.kr/webapi/cancel_process.jsp",
                cancelParams,
                Map.class
            );

            // 응답 로그 기록
            requestLog.setResponsePayload(response.toString());

            boolean isSuccess = "2001".equals(response.get("ResultCode")); // 나이스페이 취소 성공 코드

            paymentInterfaceRequestLogMapper.insert(requestLog);

            if (!isSuccess) {
                String errorMessage = "나이스페이 결제 취소 실패: " + response.get("ResultMsg");
                log.error("NicePay cancel failed: {}", errorMessage);
                throw new RuntimeException(errorMessage);
            }

            log.info("NicePay payment cancelled successfully: {}", request.getTransactionId());

        } catch (Exception e) {
            log.error("Failed to cancel NicePay payment: {}", e.getMessage(), e);

            // 에러 로그 기록
            if (requestLog != null) {
                requestLog.setResponsePayload("ERROR: " + e.getMessage());
                paymentInterfaceRequestLogMapper.insert(requestLog);
            } else {
                // requestLog가 null인 경우 에러 로그 생성
                PaymentInterfaceRequestLog errorLog = new PaymentInterfaceRequestLog();
                errorLog.setPaymentId(request.getOrderId());
                errorLog.setRequestType("NICEPAY_CANCEL");
                errorLog.setRequestPayload("Request failed before API call: " + e.getMessage());
                errorLog.setResponsePayload("ERROR: " + e.getMessage());
                errorLog.setTimestamp(LocalDateTime.now());
                paymentInterfaceRequestLogMapper.insert(errorLog);
            }

            throw new RuntimeException("나이스페이 결제 취소 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public void netCancel(PaymentNetCancelRequest request) {
        try {
            log.info("NicePay net cancelling payment for transaction: {}", request.getTid());

            // 나이스페이 망취소 API 파라미터 구성
            String tid = request.getTid();
            String authToken = request.getAuthToken();
            String amt = request.getAmount().toString();
            String ediDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

            // SignData 생성: hex(sha256(AuthToken + MID + Amt + EdiDate + MerchantKey))
            String signData = generateConfirmSignData(authToken, mid, amt, ediDate, merchantKey);

            // NicePayNetCancelRequest DTO 생성
            NicePayNetCancelRequest netCancelRequest = new NicePayNetCancelRequest();
            netCancelRequest.setTID(tid);              // 30 byte 필수 거래 ID
            netCancelRequest.setAuthToken(authToken);  // 40 byte 필수 인증 TOKEN
            netCancelRequest.setMID(mid);              // 10 byte 필수 가맹점 ID
            netCancelRequest.setAmt(amt);              // 12 byte 금액
            netCancelRequest.setEdiDate(ediDate);      // 14 byte 필수 전문생성일시
            netCancelRequest.setNetCancel("1");        // 1 byte 필수 1 고정 (망취소 여부)
            netCancelRequest.setSignData(signData);    // 256 byte 필수 서명데이터

            log.info("NicePay net cancel request: {}", netCancelRequest);

            // NetCancelURL 사용
            String netCancelUrl = request.getNetCancelUrl();
            log.info("NicePay net cancel URL: {}", netCancelUrl);

            // 나이스페이 망취소 API 호출
            String response = webClientUtil.postDtoForText(
                netCancelUrl,
                netCancelRequest
            );

            log.info("NicePay net cancel response: {}", response);

        } catch (Exception e) {
            log.error("Failed to net cancel NicePay payment: {}", e.getMessage(), e);
            throw new RuntimeException("나이스페이 망취소 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean supports(String pgCompany) {
        return PgCompany.NICEPAY.getCode().equals(pgCompany);
    }

    private String generatePaymentId() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long sequence = System.currentTimeMillis() % 100000000L;
        return dateStr + "P" + String.format("%08d", sequence);
    }

    /**
     * NicePay SignData 생성 (SHA256) - 결제 초기화용
     * 파라미터 순서: EdiDate + MID + Amt + MerchantKey
     */
    private String generateSignData(String mid, String amt, String ediDate, String merchantKey) {
        try {
            String signString = ediDate + mid + amt + merchantKey;
            log.info("NicePay Init SignString: {}", signString);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(signString.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            String result = hexString.toString();
            log.info("NicePay Init SignData: {}", result);
            return result;

        } catch (Exception e) {
            log.error("Failed to generate SignData", e);
            throw new RuntimeException("SignData 생성 실패", e);
        }
    }

    /**
     * NicePay Confirm SignData 생성 (SHA256) - 승인용
     * 파라미터 순서: AuthToken + MID + Amt + EdiDate + MerchantKey
     */
    private String generateConfirmSignData(String authToken, String mid, String amt, String ediDate, String merchantKey) {
        try {
            String signString = authToken + mid + amt + ediDate + merchantKey;
            log.info("NicePay Confirm SignString: {}", signString);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(signString.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            String result = hexString.toString();
            log.info("NicePay Confirm SignData: {}", result);
            return result;

        } catch (Exception e) {
            log.error("Failed to generate Confirm SignData", e);
            throw new RuntimeException("Confirm SignData 생성 실패", e);
        }
    }

    /**
     * NicePay 응답 Signature 검증용 (SHA256)
     * 파라미터 순서: TID + MID + Amt + MerchantKey
     */
    private String generateResponseSignature(String tid, String mid, String amt, String merchantKey) {
        try {
            String signString = tid + mid + amt + merchantKey;
            log.info("NicePay Response SignString: {}", signString);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(signString.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            String result = hexString.toString();
            log.info("NicePay Response Signature: {}", result);
            return result;

        } catch (Exception e) {
            log.error("Failed to generate Response Signature", e);
            throw new RuntimeException("Response Signature 생성 실패", e);
        }
    }

    /**
     * NicePay Cancel SignData 생성 (SHA256) - 취소용
     * 파라미터 순서: MID + CancelAmt + EdiDate + MerchantKey
     */
    private String generateCancelSignData(String mid, String cancelAmt, String ediDate, String merchantKey) {
        try {
            String signString = mid + cancelAmt + ediDate + merchantKey;
            log.info("NicePay Cancel SignString: {}", signString);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(signString.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            String result = hexString.toString();
            log.info("NicePay Cancel SignData: {}", result);
            return result;

        } catch (Exception e) {
            log.error("Failed to generate Cancel SignData", e);
            throw new RuntimeException("Cancel SignData 생성 실패", e);
        }
    }

    /**
     * NicePay 응답에서 값 추출 (key=value 형식) - 호환성 유지용
     */
    private String extractValue(String response, String key) {
        if (response == null || key == null) {
            return null;
        }

        String searchKey = key + "=";
        int startIndex = response.indexOf(searchKey);
        if (startIndex == -1) {
            return null;
        }

        startIndex += searchKey.length();
        int endIndex = response.indexOf("&", startIndex);
        if (endIndex == -1) {
            endIndex = response.length();
        }

        return response.substring(startIndex, endIndex);
    }
}