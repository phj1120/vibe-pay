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
import java.util.HashMap;
import java.util.Map;

/**
 * 포인트 결제 처리 어댑터
 *
 * 외부 PG사를 사용하지 않고 내부 포인트 시스템을 통한 결제를 처리합니다.
 * PaymentGatewayAdapter 인터페이스를 구현하여 PG사 어댑터들과 동일한 방식으로 사용됩니다.
 *
 * 포인트 결제 흐름:
 * 1. initiate: 포인트 사용 가능 여부 확인
 * 2. confirm: 포인트 차감 및 결제 완료 처리
 * 3. cancel: 포인트 환급 처리
 *
 * Technical Specification 참조:
 * - docs/v5/TechnicalSpecification/payment-and-pg-integration-spec.md
 *
 * @see PaymentGatewayAdapter
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class PointPaymentProcessor implements PaymentGatewayAdapter {

    private final PaymentInterfaceRequestLogMapper logMapper;
    private final ObjectMapper objectMapper;

    // TODO: 실제 운영 시 PointService 주입 필요
    // private final PointService pointService;

    @Override
    public boolean supports(PgCompany pgCompany) {
        return PgCompany.POINT.equals(pgCompany);
    }

    @Override
    public PaymentInitResponse initiate(PaymentInitiateRequest request) {
        log.info("Initiating POINT payment: orderId={}, amount={}, memberId={}",
                request.getOrderId(), request.getAmount(), request.getMemberId());

        try {
            // TODO: 실제 포인트 결제 초기화 구현
            // 1. 회원의 포인트 잔액 조회 (PointService.getBalance())
            // 2. 포인트 잔액이 결제 금액보다 많은지 확인
            // 3. 포인트 사용 가능 여부 확인 (회원 상태, 포인트 유효기간 등)
            // 4. 포인트 결제 가능 여부를 응답에 포함

            // Stub 구현: 실제 PointService 연동으로 대체
            Map<String, String> parameters = new HashMap<>();
            parameters.put("memberId", String.valueOf(request.getMemberId()));
            parameters.put("orderId", request.getOrderId());
            parameters.put("amount", request.getAmount().toString());
            parameters.put("availablePoints", "100000"); // 실제로는 PointService에서 조회

            PaymentInitResponse response = new PaymentInitResponse();
            response.setSuccess(true);
            response.setPaymentUrl("INTERNAL_POINT_SYSTEM");
            response.setParameters(parameters);
            response.setMessage("Point payment initiation successful (stub)");

            log.info("POINT payment initiation completed (stub): orderId={}, memberId={}",
                    request.getOrderId(), request.getMemberId());
            return response;

        } catch (Exception e) {
            log.error("POINT payment initiation failed: orderId={}", request.getOrderId(), e);
            throw new RuntimeException("POINT payment initiation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentConfirmResponse confirm(PaymentConfirmRequest request) {
        log.info("Confirming POINT payment: orderId={}, amount={}, memberId={}",
                request.getOrderId(), request.getAmount(), request.getMemberId());

        try {
            // TODO: 실제 포인트 결제 승인 구현
            // 1. PaymentInterfaceRequestLogMapper에 요청 로그 기록
            // 2. 회원의 포인트 잔액 재확인 (동시성 제어)
            // 3. 포인트 차감 (PointService.deduct())
            //    - 포인트 트랜잭션 기록 생성
            //    - 회원 포인트 잔액 업데이트
            // 4. PaymentInterfaceRequestLogMapper에 응답 로그 기록
            // 5. PaymentConfirmResponse 구성

            // Stub 구현: 실제 PointService 연동으로 대체
            PaymentConfirmResponse response = new PaymentConfirmResponse();
            response.setSuccess(true);
            response.setTransactionId("POINT_" + System.currentTimeMillis());
            response.setApprovalNumber("POINT_APPROVAL_" + System.currentTimeMillis());
            response.setMessage("Point payment confirmation successful (stub)");
            response.setAmount(request.getAmount());
            response.setResultCode("0000");

            log.info("POINT payment confirmation completed (stub): orderId={}, transactionId={}, memberId={}",
                    request.getOrderId(), response.getTransactionId(), request.getMemberId());

            return response;

        } catch (Exception e) {
            log.error("POINT payment confirmation failed: orderId={}", request.getOrderId(), e);
            throw new RuntimeException("POINT payment confirmation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentCancelResponse cancel(PaymentCancelRequest request) {
        log.info("Cancelling POINT payment: orderId={}, paymentId={}, amount={}",
                request.getOrderId(), request.getPaymentId(), request.getAmount());

        try {
            // 1. 요청 로그 기록
            PaymentInterfaceRequestLog requestLog = new PaymentInterfaceRequestLog();
            requestLog.setPaymentId(request.getPaymentId());
            requestLog.setRequestType("CANCEL");
            requestLog.setRequestPayload(objectMapper.writeValueAsString(request));
            requestLog.setTimestamp(LocalDateTime.now());
            logMapper.insert(requestLog);

            // 2. 포인트 환급 처리
            // TODO: 실제 구현
            // - 원본 포인트 차감 트랜잭션 조회
            // - 포인트 환급 (PointService.refund())
            //   * 포인트 환급 트랜잭션 기록 생성
            //   * 회원 포인트 잔액 업데이트
            // - 환급 사유 기록

            // Stub 구현: 실제 PointService 연동으로 대체
            PaymentCancelResponse response = new PaymentCancelResponse();
            response.setSuccess(true);
            response.setCancelTransactionId("POINT_CANCEL_" + System.currentTimeMillis());
            response.setCancelApprovalNumber("CANCEL_APPROVAL_" + System.currentTimeMillis());
            response.setCancelAmount(request.getAmount());
            response.setResultCode("0000");
            response.setMessage("Point payment cancellation successful (stub)");
            response.setOriginalTransactionId(request.getOriginalTransactionId());

            // 3. 응답 로그 기록
            requestLog.setResponsePayload(objectMapper.writeValueAsString(response));
            logMapper.updateResponse(requestLog);

            // 4. 응답 검증
            if (!response.isSuccess()) {
                throw new RuntimeException("Point cancellation failed: " + response.getMessage());
            }

            if (!response.getCancelAmount().equals(request.getAmount())) {
                throw new RuntimeException("Cancel amount mismatch: requested="
                        + request.getAmount() + ", actual=" + response.getCancelAmount());
            }

            log.info("POINT payment cancellation completed: orderId={}, cancelTransactionId={}",
                    request.getOrderId(), response.getCancelTransactionId());

            return response;

        } catch (Exception e) {
            log.error("POINT payment cancellation failed: orderId={}", request.getOrderId(), e);
            throw new RuntimeException("POINT payment cancellation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentCancelResponse netCancel(PaymentCancelRequest request) {
        log.info("Net cancelling POINT payment: orderId={}, paymentId={}, amount={}",
                request.getOrderId(), request.getPaymentId(), request.getAmount());

        try {
            // 1. 요청 로그 기록
            PaymentInterfaceRequestLog requestLog = new PaymentInterfaceRequestLog();
            requestLog.setPaymentId(request.getPaymentId());
            requestLog.setRequestType("NET_CANCEL");
            requestLog.setRequestPayload(objectMapper.writeValueAsString(request));
            requestLog.setTimestamp(LocalDateTime.now());
            logMapper.insert(requestLog);

            // 2. 포인트 망취소 처리
            // 포인트 결제는 cancel()과 동일하게 처리
            // TODO: 실제 구현
            // - cancel() 메서드와 동일한 방식으로 포인트 환급
            // - 취소 사유를 "시스템 오류로 인한 자동 취소"로 기록

            // Stub 구현: 실제 PointService 연동으로 대체
            PaymentCancelResponse response = new PaymentCancelResponse();
            response.setSuccess(true);
            response.setCancelTransactionId("POINT_NET_CANCEL_" + System.currentTimeMillis());
            response.setCancelApprovalNumber("NET_CANCEL_APPROVAL_" + System.currentTimeMillis());
            response.setCancelAmount(request.getAmount());
            response.setResultCode("0000");
            response.setMessage("Point payment net cancellation successful (stub)");
            response.setOriginalTransactionId(request.getOriginalTransactionId());

            // 3. 응답 로그 기록
            requestLog.setResponsePayload(objectMapper.writeValueAsString(response));
            logMapper.updateResponse(requestLog);

            // 4. 응답 검증
            if (!response.isSuccess()) {
                throw new RuntimeException("Point net cancellation failed: " + response.getMessage());
            }

            log.info("POINT payment net cancellation completed: orderId={}, cancelTransactionId={}",
                    request.getOrderId(), response.getCancelTransactionId());

            return response;

        } catch (Exception e) {
            log.error("POINT payment net cancellation failed: orderId={}", request.getOrderId(), e);
            throw new RuntimeException("POINT payment net cancellation failed: " + e.getMessage(), e);
        }
    }
}
