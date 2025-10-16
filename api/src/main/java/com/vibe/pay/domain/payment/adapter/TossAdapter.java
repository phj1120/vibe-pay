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
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 토스페이먼츠 PG 연동 어댑터
 *
 * 토스페이먼츠 PG사와의 결제 초기화, 승인, 취소 API 연동을 담당합니다.
 * 토스페이먼츠는 RESTful API 방식을 사용하며, 최신 PG사 중 하나입니다.
 *
 * Technical Specification 참조:
 * - docs/v5/TechnicalSpecification/payment-and-pg-integration-spec.md
 * - https://docs.tosspayments.com/reference
 *
 * @see PaymentGatewayAdapter
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TossAdapter implements PaymentGatewayAdapter {

    private final PaymentInterfaceRequestLogMapper logMapper;
    private final ObjectMapper objectMapper;

    // 토스페이먼츠 설정 (실제 운영 시 application.yml에서 관리)
    private static final String TOSS_PAYMENT_URL = "https://api.tosspayments.com/v1/payments";
    private static final String TOSS_CONFIRM_URL = "https://api.tosspayments.com/v1/payments/confirm";
    private static final String TOSS_CANCEL_URL = "https://api.tosspayments.com/v1/payments/{paymentKey}/cancel";
    private static final String CLIENT_KEY = "test_ck_D5GePWvyJnrK0W0k6q8gLzN97Eoq"; // 실제 운영 시 환경변수에서 관리
    private static final String SECRET_KEY = "test_sk_zXLkKEypNArWmo50nX3lmeaxYG5R"; // 실제 운영 시 환경변수에서 관리

    @Override
    public boolean supports(PgCompany pgCompany) {
        return PgCompany.TOSS.equals(pgCompany);
    }

    @Override
    public PaymentInitResponse initiate(PaymentInitiateRequest request) {
        log.info("Initiating TOSS payment: orderId={}, amount={}",
                request.getOrderId(), request.getAmount());

        try {
            // TODO: 실제 토스페이먼츠 결제 초기화 구현
            // 토스페이먼츠는 클라이언트 SDK를 사용하므로 백엔드에서는 결제창 파라미터만 제공
            // 1. 클라이언트 키 제공
            // 2. 결제 정보 (orderId, amount, orderName, customerName, customerEmail) 제공

            // 결제창 파라미터 생성
            Map<String, String> parameters = new HashMap<>();
            parameters.put("clientKey", CLIENT_KEY);
            parameters.put("orderId", request.getOrderId());
            parameters.put("amount", request.getAmount().toString());
            parameters.put("orderName", request.getProductName());
            parameters.put("customerName", request.getBuyerName());
            parameters.put("customerEmail", request.getBuyerEmail());
            parameters.put("successUrl", request.getReturnUrl());
            parameters.put("failUrl", request.getCancelUrl());

            // 응답 생성
            PaymentInitResponse response = new PaymentInitResponse();
            response.setSuccess(true);
            response.setPaymentUrl(TOSS_PAYMENT_URL);
            response.setParameters(parameters);
            response.setMessage("TOSS payment initiation successful (stub)");

            log.info("TOSS payment initiation completed (stub): orderId={}", request.getOrderId());
            return response;

        } catch (Exception e) {
            log.error("TOSS payment initiation failed: orderId={}", request.getOrderId(), e);
            throw new RuntimeException("TOSS payment initiation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentConfirmResponse confirm(PaymentConfirmRequest request) {
        log.info("Confirming TOSS payment: orderId={}, amount={}",
                request.getOrderId(), request.getAmount());

        try {
            // TODO: 실제 토스페이먼츠 승인 API 연동 구현
            // 1. PaymentInterfaceRequestLogMapper에 요청 로그 기록
            // 2. Basic Auth 헤더 생성 (Base64.encode(SECRET_KEY + ":"))
            // 3. 토스페이먼츠 승인 API 호출 (WebClientUtil 활용)
            //    - Endpoint: POST /v1/payments/confirm
            //    - Headers: Authorization: Basic {encodedSecretKey}, Content-Type: application/json
            //    - Body: { "paymentKey": "{paymentKey}", "orderId": "{orderId}", "amount": {amount} }
            // 4. PaymentInterfaceRequestLogMapper에 응답 로그 기록
            // 5. 응답 검증 (status == "DONE", 금액 일치 여부)
            // 6. PaymentConfirmResponse 구성

            // Stub 구현
            PaymentConfirmResponse response = new PaymentConfirmResponse();
            response.setSuccess(true);
            response.setTransactionId("TOSS_" + System.currentTimeMillis());
            response.setApprovalNumber("TOSS_APPROVAL_" + System.currentTimeMillis());
            response.setMessage("TOSS payment confirmation successful (stub)");
            response.setAmount(request.getAmount());
            response.setResultCode("DONE");

            log.info("TOSS payment confirmation completed (stub): orderId={}, transactionId={}",
                    request.getOrderId(), response.getTransactionId());

            return response;

        } catch (Exception e) {
            log.error("TOSS payment confirmation failed: orderId={}", request.getOrderId(), e);
            throw new RuntimeException("TOSS payment confirmation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentCancelResponse cancel(PaymentCancelRequest request) {
        log.info("Cancelling TOSS payment: orderId={}, paymentId={}, amount={}",
                request.getOrderId(), request.getPaymentId(), request.getAmount());

        try {
            // 1. 요청 로그 기록
            PaymentInterfaceRequestLog requestLog = new PaymentInterfaceRequestLog();
            requestLog.setPaymentId(request.getPaymentId());
            requestLog.setRequestType("CANCEL");
            requestLog.setRequestPayload(objectMapper.writeValueAsString(request));
            requestLog.setTimestamp(LocalDateTime.now());
            logMapper.insert(requestLog);

            // 2. 토스페이먼츠 취소 API 호출 (실제 구현 시 WebClient 사용)
            // TODO: 실제 구현
            // - Endpoint: POST /v1/payments/{paymentKey}/cancel
            // - Headers: Authorization: Basic {encodedSecretKey}, Content-Type: application/json
            // - Body: { "cancelReason": "{cancelReason}", "cancelAmount": {amount} }
            // - Response: { "status": "CANCELED", "cancels": [...] }

            // Stub 구현: 실제 PG 연동 시 WebClient를 통한 HTTP 호출로 대체
            PaymentCancelResponse response = new PaymentCancelResponse();
            response.setSuccess(true);
            response.setCancelTransactionId("TOSS_CANCEL_" + System.currentTimeMillis());
            response.setCancelApprovalNumber("CANCEL_APPROVAL_" + System.currentTimeMillis());
            response.setCancelAmount(request.getAmount());
            response.setResultCode("CANCELED");
            response.setMessage("TOSS payment cancellation successful (stub)");
            response.setOriginalTransactionId(request.getOriginalTransactionId());

            // 3. 응답 로그 기록
            requestLog.setResponsePayload(objectMapper.writeValueAsString(response));
            logMapper.updateResponse(requestLog);

            // 4. 응답 검증
            if (!response.isSuccess()) {
                throw new RuntimeException("TOSS cancellation failed: " + response.getMessage());
            }

            if (!response.getCancelAmount().equals(request.getAmount())) {
                throw new RuntimeException("Cancel amount mismatch: requested="
                        + request.getAmount() + ", actual=" + response.getCancelAmount());
            }

            log.info("TOSS payment cancellation completed: orderId={}, cancelTransactionId={}",
                    request.getOrderId(), response.getCancelTransactionId());

            return response;

        } catch (Exception e) {
            log.error("TOSS payment cancellation failed: orderId={}", request.getOrderId(), e);
            throw new RuntimeException("TOSS payment cancellation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentCancelResponse netCancel(PaymentCancelRequest request) {
        log.info("Net cancelling TOSS payment: orderId={}, paymentId={}, amount={}",
                request.getOrderId(), request.getPaymentId(), request.getAmount());

        try {
            // 1. 요청 로그 기록
            PaymentInterfaceRequestLog requestLog = new PaymentInterfaceRequestLog();
            requestLog.setPaymentId(request.getPaymentId());
            requestLog.setRequestType("NET_CANCEL");
            requestLog.setRequestPayload(objectMapper.writeValueAsString(request));
            requestLog.setTimestamp(LocalDateTime.now());
            logMapper.insert(requestLog);

            // 2. 토스페이먼츠 망취소 API 호출
            // 토스페이먼츠는 일반 취소와 동일한 엔드포인트 사용
            // TODO: 실제 구현
            // - cancel() 메서드와 동일한 방식으로 호출
            // - cancelReason을 "시스템 오류로 인한 자동 취소"로 설정

            // Stub 구현: 실제 PG 연동 시 WebClient를 통한 HTTP 호출로 대체
            PaymentCancelResponse response = new PaymentCancelResponse();
            response.setSuccess(true);
            response.setCancelTransactionId("TOSS_NET_CANCEL_" + System.currentTimeMillis());
            response.setCancelApprovalNumber("NET_CANCEL_APPROVAL_" + System.currentTimeMillis());
            response.setCancelAmount(request.getAmount());
            response.setResultCode("CANCELED");
            response.setMessage("TOSS payment net cancellation successful (stub)");
            response.setOriginalTransactionId(request.getOriginalTransactionId());

            // 3. 응답 로그 기록
            requestLog.setResponsePayload(objectMapper.writeValueAsString(response));
            logMapper.updateResponse(requestLog);

            // 4. 응답 검증
            if (!response.isSuccess()) {
                throw new RuntimeException("TOSS net cancellation failed: " + response.getMessage());
            }

            log.info("TOSS payment net cancellation completed: orderId={}, cancelTransactionId={}",
                    request.getOrderId(), response.getCancelTransactionId());

            return response;

        } catch (Exception e) {
            log.error("TOSS payment net cancellation failed: orderId={}", request.getOrderId(), e);
            throw new RuntimeException("TOSS payment net cancellation failed: " + e.getMessage(), e);
        }
    }

    /**
     * 토스페이먼츠 Basic Auth 헤더 생성
     *
     * @return Base64 인코딩된 인증 문자열
     */
    private String generateAuthHeader() {
        // TODO: 실제 Base64 인코딩 구현
        // Base64.getEncoder().encodeToString((SECRET_KEY + ":").getBytes())
        return "Basic " + Base64.getEncoder().encodeToString((SECRET_KEY + ":").getBytes());
    }
}
