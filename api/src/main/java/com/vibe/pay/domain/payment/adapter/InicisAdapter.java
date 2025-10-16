package com.vibe.pay.domain.payment.adapter;

import com.vibe.pay.domain.payment.dto.PaymentCancelRequest;
import com.vibe.pay.domain.payment.dto.PaymentConfirmRequest;
import com.vibe.pay.domain.payment.dto.PaymentConfirmResponse;
import com.vibe.pay.domain.payment.dto.PaymentInitResponse;
import com.vibe.pay.domain.payment.dto.PaymentInitiateRequest;
import com.vibe.pay.domain.payment.repository.PaymentInterfaceRequestLogMapper;
import com.vibe.pay.enums.PgCompany;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 이니시스 PG 연동 어댑터
 *
 * 이니시스 PG사와의 결제 초기화, 승인, 취소 API 연동을 담당합니다.
 *
 * Technical Specification 참조:
 * - docs/v5/TechnicalSpecification/payment-and-pg-integration-spec.md
 * - docs/v5/TechnicalSpecification/inicis-api-spec.md
 *
 * @see PaymentGatewayAdapter
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class InicisAdapter implements PaymentGatewayAdapter {

    private final PaymentInterfaceRequestLogMapper logMapper;

    @Override
    public boolean supports(PgCompany pgCompany) {
        return PgCompany.INICIS.equals(pgCompany);
    }

    @Override
    public PaymentInitResponse initiate(PaymentInitiateRequest request) {
        log.info("Initiating INICIS payment: orderId={}, amount={}",
                request.getOrderId(), request.getAmount());

        // TODO: 실제 이니시스 결제 초기화 API 연동 구현
        // 1. 이니시스 결제창 파라미터 생성 (mid, oid, price, etc.)
        // 2. 서명(signature) 생성
        // 3. PaymentInitResponse 구성 (결제창 URL, 파라미터)

        // Stub 구현
        PaymentInitResponse response = new PaymentInitResponse();
        response.setSuccess(true);
        response.setPaymentUrl("https://stgstdpay.inicis.com/stdpay/pay");
        response.setMessage("INICIS payment initiation successful (stub)");

        log.info("INICIS payment initiation completed (stub): orderId={}", request.getOrderId());
        return response;
    }

    @Override
    public PaymentConfirmResponse confirm(PaymentConfirmRequest request) {
        log.info("Confirming INICIS payment: orderId={}, amount={}",
                request.getOrderId(), request.getAmount());

        try {
            // TODO: 실제 이니시스 승인 API 연동 구현
            // 1. PaymentInterfaceRequestLogMapper에 요청 로그 기록
            // 2. 이니시스 승인 API 호출 (WebClientUtil 활용)
            // 3. PaymentInterfaceRequestLogMapper에 응답 로그 기록
            // 4. 응답 검증 (resultCode, 금액 일치 여부)
            // 5. PaymentConfirmResponse 구성

            // Stub 구현
            PaymentConfirmResponse response = new PaymentConfirmResponse();
            response.setSuccess(true);
            response.setTransactionId("INICIS_" + System.currentTimeMillis());
            response.setApprovalNumber("INICIS_APPROVAL_" + System.currentTimeMillis());
            response.setMessage("INICIS payment confirmation successful (stub)");
            response.setAmount(request.getAmount());

            log.info("INICIS payment confirmation completed (stub): orderId={}, transactionId={}",
                    request.getOrderId(), response.getTransactionId());

            return response;

        } catch (Exception e) {
            log.error("INICIS payment confirmation failed: orderId={}", request.getOrderId(), e);
            throw new RuntimeException("INICIS payment confirmation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void cancel(PaymentCancelRequest request) {
        log.info("Cancelling INICIS payment: orderId={}, amount={}",
                request.getOrderId(), request.getAmount());

        try {
            // TODO: 실제 이니시스 취소 API 연동 구현
            // 1. PaymentInterfaceRequestLogMapper에 요청 로그 기록
            // 2. 이니시스 취소 API 호출
            // 3. PaymentInterfaceRequestLogMapper에 응답 로그 기록
            // 4. 응답 검증

            log.info("INICIS payment cancellation completed (stub): orderId={}", request.getOrderId());

        } catch (Exception e) {
            log.error("INICIS payment cancellation failed: orderId={}", request.getOrderId(), e);
            throw new RuntimeException("INICIS payment cancellation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void netCancel(PaymentCancelRequest request) {
        log.info("Net cancelling INICIS payment: orderId={}, amount={}",
                request.getOrderId(), request.getAmount());

        try {
            // TODO: 실제 이니시스 망취소 API 연동 구현
            // 망취소는 승인 후 주문 생성 실패 시 자동으로 처리되는 취소
            // 일반 취소와 API가 다를 수 있음

            log.info("INICIS payment net cancellation completed (stub): orderId={}", request.getOrderId());

        } catch (Exception e) {
            log.error("INICIS payment net cancellation failed: orderId={}", request.getOrderId(), e);
            throw new RuntimeException("INICIS payment net cancellation failed: " + e.getMessage(), e);
        }
    }
}