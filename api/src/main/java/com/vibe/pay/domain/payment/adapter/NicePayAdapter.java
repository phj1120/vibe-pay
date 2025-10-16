package com.vibe.pay.domain.payment.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibe.pay.domain.payment.dto.PaymentCancelRequest;
import com.vibe.pay.domain.payment.dto.PaymentCancelResponse;
import com.vibe.pay.domain.payment.dto.PaymentConfirmRequest;
import com.vibe.pay.domain.payment.dto.PaymentConfirmResponse;
import com.vibe.pay.domain.payment.dto.PaymentInitResponse;
import com.vibe.pay.domain.payment.dto.PaymentInitiateRequest;
import com.vibe.pay.domain.payment.entity.PaymentInterfaceRequestLog;
import com.vibe.pay.domain.payment.repository.PaymentInterfaceRequestLogMapper;
import com.vibe.pay.enums.PgCompany;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 나이스페이 PG 연동 어댑터
 *
 * 나이스페이 PG사와의 결제 초기화, 승인, 취소 API 연동을 담당합니다.
 *
 * Technical Specification 참조:
 * - docs/v5/TechnicalSpecification/payment-and-pg-integration-spec.md
 * - docs/v5/TechnicalSpecification/nicepay-api-spec.md
 *
 * @see PaymentGatewayAdapter
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class NicePayAdapter implements PaymentGatewayAdapter {

    private final PaymentInterfaceRequestLogMapper logMapper;
    private final ObjectMapper objectMapper;

    // 나이스페이 설정 (실제 운영 시 application.yml에서 관리)
    private static final String NICEPAY_PAYMENT_URL = "https://web.nicepay.co.kr/v3/v3Payment.jsp";
    private static final String NICEPAY_CONFIRM_URL = "https://webapi.nicepay.co.kr/v3/v3Payment.jsp";
    private static final String NICEPAY_CANCEL_URL = "https://webapi.nicepay.co.kr/webapi/cancel_process.jsp";
    private static final String MID = "nicepayTest01"; // 실제 운영 시 환경변수에서 관리
    private static final String MERCHANT_KEY = "nicepayMerchantKey"; // 실제 운영 시 환경변수에서 관리

    @Override
    public boolean supports(PgCompany pgCompany) {
        return PgCompany.NICEPAY.equals(pgCompany);
    }

    @Override
    public PaymentInitResponse initiate(PaymentInitiateRequest request) {
        log.info("Initiating NICEPAY payment: orderId={}, amount={}",
                request.getOrderId(), request.getAmount());

        try {
            // TODO: 실제 나이스페이 결제 초기화 구현
            // 1. 전문 생성 일시 (ediDate) 생성
            String ediDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

            // 2. 서명 데이터 생성 (SHA-256)
            // SignData = SHA-256(EdiDate + MID + Amt + MerchantKey)
            String signData = generateSignData(ediDate, MID, request.getAmount().toString(), MERCHANT_KEY);

            // 3. 결제창 파라미터 생성
            Map<String, String> parameters = new HashMap<>();
            parameters.put("mid", MID);
            parameters.put("moid", request.getOrderId());
            parameters.put("amt", request.getAmount().toString());
            parameters.put("goodName", request.getProductName());
            parameters.put("buyerName", request.getBuyerName());
            parameters.put("buyerEmail", request.getBuyerEmail());
            parameters.put("buyerTel", request.getBuyerPhone());
            parameters.put("ediDate", ediDate);
            parameters.put("SignData", signData);
            parameters.put("returnUrl", request.getReturnUrl());
            parameters.put("cancelUrl", request.getCancelUrl());
            parameters.put("version", "1.0");
            parameters.put("currency", "KRW");

            // 4. 응답 생성
            PaymentInitResponse response = new PaymentInitResponse();
            response.setSuccess(true);
            response.setPaymentUrl(NICEPAY_PAYMENT_URL);
            response.setParameters(parameters);
            response.setMessage("NICEPAY payment initiation successful (stub)");

            log.info("NICEPAY payment initiation completed (stub): orderId={}", request.getOrderId());
            return response;

        } catch (Exception e) {
            log.error("NICEPAY payment initiation failed: orderId={}", request.getOrderId(), e);
            throw new RuntimeException("NICEPAY payment initiation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentConfirmResponse confirm(PaymentConfirmRequest request) {
        log.info("Confirming NICEPAY payment: orderId={}, amount={}",
                request.getOrderId(), request.getAmount());

        try {
            // TODO: 실제 나이스페이 승인 API 연동 구현
            // 1. PaymentInterfaceRequestLogMapper에 요청 로그 기록
            // 2. 전문 생성 일시 (ediDate) 생성
            // 3. 서명 데이터 생성 (AuthToken + MID + Amt + EdiDate + MerchantKey)
            // 4. 나이스페이 승인 API 호출 (WebClientUtil 활용)
            //    - Endpoint: NICEPAY_CONFIRM_URL (NextAppURL에서 전달받음)
            //    - Parameters: TID, AuthToken, MID, Amt, EdiDate, SignData
            // 5. PaymentInterfaceRequestLogMapper에 응답 로그 기록
            // 6. 응답 검증 (resultCode == "3001", 응답 Signature 검증)
            // 7. PaymentConfirmResponse 구성

            // Stub 구현
            PaymentConfirmResponse response = new PaymentConfirmResponse();
            response.setSuccess(true);
            response.setTransactionId("NICEPAY_" + System.currentTimeMillis());
            response.setApprovalNumber("NICEPAY_APPROVAL_" + System.currentTimeMillis());
            response.setMessage("NICEPAY payment confirmation successful (stub)");
            response.setAmount(request.getAmount());
            response.setResultCode("3001");

            log.info("NICEPAY payment confirmation completed (stub): orderId={}, transactionId={}",
                    request.getOrderId(), response.getTransactionId());

            return response;

        } catch (Exception e) {
            log.error("NICEPAY payment confirmation failed: orderId={}", request.getOrderId(), e);
            throw new RuntimeException("NICEPAY payment confirmation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentCancelResponse cancel(PaymentCancelRequest request) {
        log.info("Cancelling NICEPAY payment: orderId={}, paymentId={}, amount={}",
                request.getOrderId(), request.getPaymentId(), request.getAmount());

        try {
            // 1. 요청 로그 기록
            PaymentInterfaceRequestLog requestLog = new PaymentInterfaceRequestLog();
            requestLog.setPaymentId(request.getPaymentId());
            requestLog.setRequestType("CANCEL");
            requestLog.setRequestPayload(objectMapper.writeValueAsString(request));
            requestLog.setTimestamp(LocalDateTime.now());
            logMapper.insert(requestLog);

            // 2. 나이스페이 취소 API 호출 (실제 구현 시 WebClient 사용)
            // TODO: 실제 구현
            // - Endpoint: NICEPAY_CANCEL_URL
            // - Parameters: TID, MID, CancelAmt, CancelMsg, PartialCancelCode, EdiDate, SignData, CharSet, Moid
            // - SignData = SHA-256(MID + CancelAmt + EdiDate + MerchantKey)
            // - resultCode == "2001"이면 성공

            // Stub 구현: 실제 PG 연동 시 WebClient를 통한 HTTP 호출로 대체
            PaymentCancelResponse response = new PaymentCancelResponse();
            response.setSuccess(true);
            response.setCancelTransactionId("NICEPAY_CANCEL_" + System.currentTimeMillis());
            response.setCancelApprovalNumber("CANCEL_APPROVAL_" + System.currentTimeMillis());
            response.setCancelAmount(request.getAmount());
            response.setResultCode("2001");
            response.setMessage("NICEPAY payment cancellation successful (stub)");
            response.setOriginalTransactionId(request.getOriginalTransactionId());

            // 3. 응답 로그 기록
            requestLog.setResponsePayload(objectMapper.writeValueAsString(response));
            logMapper.updateResponse(requestLog);

            // 4. 응답 검증
            if (!response.isSuccess()) {
                throw new RuntimeException("NICEPAY cancellation failed: " + response.getMessage());
            }

            if (!response.getCancelAmount().equals(request.getAmount())) {
                throw new RuntimeException("Cancel amount mismatch: requested="
                        + request.getAmount() + ", actual=" + response.getCancelAmount());
            }

            log.info("NICEPAY payment cancellation completed: orderId={}, cancelTransactionId={}",
                    request.getOrderId(), response.getCancelTransactionId());

            return response;

        } catch (Exception e) {
            log.error("NICEPAY payment cancellation failed: orderId={}", request.getOrderId(), e);
            throw new RuntimeException("NICEPAY payment cancellation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentCancelResponse netCancel(PaymentCancelRequest request) {
        log.info("Net cancelling NICEPAY payment: orderId={}, paymentId={}, amount={}",
                request.getOrderId(), request.getPaymentId(), request.getAmount());

        try {
            // 1. 요청 로그 기록
            PaymentInterfaceRequestLog requestLog = new PaymentInterfaceRequestLog();
            requestLog.setPaymentId(request.getPaymentId());
            requestLog.setRequestType("NET_CANCEL");
            requestLog.setRequestPayload(objectMapper.writeValueAsString(request));
            requestLog.setTimestamp(LocalDateTime.now());
            logMapper.insert(requestLog);

            // 2. 나이스페이 망취소 API 호출
            // TODO: 실제 구현
            // - Endpoint: NextAppURL (returnUrl에서 전달받은 netCancelUrl)
            // - Parameters: TID, AuthToken, MID, Amt, EdiDate, NetCancel("1"), SignData
            // - SignData = SHA-256(AuthToken + MID + Amt + EdiDate + MerchantKey)
            // - HTTP Status 2xx이면 성공

            // Stub 구현: 실제 PG 연동 시 WebClient를 통한 HTTP 호출로 대체
            PaymentCancelResponse response = new PaymentCancelResponse();
            response.setSuccess(true);
            response.setCancelTransactionId("NICEPAY_NET_CANCEL_" + System.currentTimeMillis());
            response.setCancelApprovalNumber("NET_CANCEL_APPROVAL_" + System.currentTimeMillis());
            response.setCancelAmount(request.getAmount());
            response.setResultCode("0000");
            response.setMessage("NICEPAY payment net cancellation successful (stub)");
            response.setOriginalTransactionId(request.getOriginalTransactionId());

            // 3. 응답 로그 기록
            requestLog.setResponsePayload(objectMapper.writeValueAsString(response));
            logMapper.updateResponse(requestLog);

            // 4. 응답 검증
            if (!response.isSuccess()) {
                throw new RuntimeException("NICEPAY net cancellation failed: " + response.getMessage());
            }

            log.info("NICEPAY payment net cancellation completed: orderId={}, cancelTransactionId={}",
                    request.getOrderId(), response.getCancelTransactionId());

            return response;

        } catch (Exception e) {
            log.error("NICEPAY payment net cancellation failed: orderId={}", request.getOrderId(), e);
            throw new RuntimeException("NICEPAY payment net cancellation failed: " + e.getMessage(), e);
        }
    }

    /**
     * 나이스페이 SignData 생성 (SHA-256 해싱)
     *
     * @param parts 해싱할 문자열 파트들
     * @return SHA-256 해싱된 문자열
     */
    private String generateSignData(String... parts) {
        // TODO: 실제 SHA-256 해싱 구현
        // java.security.MessageDigest를 사용하여 SHA-256 해싱 수행
        // 예시: MessageDigest.getInstance("SHA-256").digest(String.join("", parts).getBytes())
        return "STUB_SIGN_DATA_" + String.join("_", parts).hashCode();
    }
}
