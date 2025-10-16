package com.vibe.pay.domain.payment.adapter;

import com.vibe.pay.domain.payment.dto.PaymentCancelRequest;
import com.vibe.pay.domain.payment.dto.PaymentCancelResponse;
import com.vibe.pay.domain.payment.dto.PaymentConfirmRequest;
import com.vibe.pay.domain.payment.dto.PaymentConfirmResponse;
import com.vibe.pay.domain.payment.dto.PaymentInitiateRequest;
import com.vibe.pay.domain.payment.dto.PaymentInitResponse;
import com.vibe.pay.enums.PgCompany;

/**
 * PG사별 결제 연동을 위한 어댑터 인터페이스
 *
 * 각 PG사(이니시스, 나이스페이, 토스페이먼츠)별로 구현체를 제공하여
 * 결제 초기화, 승인, 취소 등의 기능을 처리합니다.
 *
 * Technical Specification 참조:
 * - docs/v5/TechnicalSpecification/payment-and-pg-integration-spec.md
 *
 * @see InicisAdapter
 * @see NicePayAdapter
 * @see TossAdapter
 */
public interface PaymentGatewayAdapter {

    /**
     * 해당 PG사를 지원하는지 확인
     *
     * @param pgCompany PG사 enum
     * @return 지원 여부
     */
    boolean supports(PgCompany pgCompany);

    /**
     * 결제 초기화 (결제창 준비)
     *
     * @param request 결제 초기화 요청
     * @return 결제 초기화 응답 (결제창 URL, 파라미터 등)
     * @throws RuntimeException PG사 연동 실패 시
     */
    PaymentInitResponse initiate(PaymentInitiateRequest request);

    /**
     * 결제 승인
     *
     * @param request 결제 승인 요청
     * @return 결제 승인 응답
     * @throws RuntimeException PG사 승인 실패 시
     */
    PaymentConfirmResponse confirm(PaymentConfirmRequest request);

    /**
     * 결제 취소/환불
     *
     * @param request 결제 취소 요청
     * @return 결제 취소 응답
     * @throws RuntimeException PG사 취소 실패 시
     */
    PaymentCancelResponse cancel(PaymentCancelRequest request);

    /**
     * 망취소 (결제 승인 후 주문 생성 실패 시)
     *
     * @param request 망취소 요청
     * @return 망취소 응답
     * @throws RuntimeException PG사 망취소 실패 시
     */
    PaymentCancelResponse netCancel(PaymentCancelRequest request);
}