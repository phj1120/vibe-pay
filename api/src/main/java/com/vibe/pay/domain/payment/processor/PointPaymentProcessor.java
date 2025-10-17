package com.vibe.pay.domain.payment.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibe.pay.domain.payment.dto.PaymentConfirmRequest;
import com.vibe.pay.domain.payment.entity.Payment;
import com.vibe.pay.domain.payment.entity.PaymentInterfaceRequestLog;
import com.vibe.pay.domain.payment.repository.PaymentInterfaceRequestLogMapper;
import com.vibe.pay.domain.payment.repository.PaymentMapper;
import com.vibe.pay.enums.PaymentMethod;
import com.vibe.pay.enums.PayType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 포인트 결제 프로세서
 *
 * 내부 포인트 시스템을 통한 결제 처리를 담당합니다.
 * 외부 PG사를 사용하지 않고 내부 포인트 잔액 차감/환급으로 결제를 처리합니다.
 *
 * 포인트 결제 흐름:
 * 1. processPayment: 포인트 차감 및 결제 완료 처리
 * 2. processRefund: 포인트 환급 처리
 *
 * Technical Specification 참조:
 * - docs/v5/TechnicalSpecification/payment-and-pg-integration-spec.md
 *
 * @see PaymentProcessor
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class PointPaymentProcessor implements PaymentProcessor {

    private final PaymentMapper paymentMapper;
    private final PaymentInterfaceRequestLogMapper logMapper;
    private final ObjectMapper objectMapper;

    // TODO: 실제 운영 시 PointService 주입 필요
    // private final PointService pointService;

    @Override
    public boolean supports(PaymentMethod paymentMethod) {
        return PaymentMethod.POINT.equals(paymentMethod);
    }

    @Override
    public Payment processPayment(PaymentConfirmRequest request) {
        log.info("Processing POINT payment: orderId={}, amount={}, memberId={}",
                request.getOrderId(), request.getAmount(), request.getMemberId());

        try {
            // 1. Payment ID 생성
            // TODO: PaymentMapper.getNextPaymentSequence() 구현 후 시퀀스 기반 ID로 변경
            String paymentId = generatePaymentId();

            // 2. 포인트 결제 처리
            // TODO: 실제 포인트 결제 승인 구현
            // - PaymentInterfaceRequestLogMapper에 요청 로그 기록
            // - 회원의 포인트 잔액 재확인 (동시성 제어)
            // - 포인트 차감 (PointService.deduct())
            //   * 포인트 트랜잭션 기록 생성
            //   * 회원 포인트 잔액 업데이트
            // - PaymentInterfaceRequestLogMapper에 응답 로그 기록

            // Stub 구현: 실제 PointService 연동으로 대체
            logPaymentRequest(request, paymentId);

            String transactionId = "POINT_" + System.currentTimeMillis();
            String approvalNumber = "POINT_APPROVAL_" + System.currentTimeMillis();

            logPaymentResponse(paymentId, transactionId, approvalNumber, request.getAmount().toString());

            // 3. Payment 엔티티 생성
            Payment payment = new Payment();
            payment.setPaymentId(paymentId);
            payment.setOrderId(request.getOrderId());
            payment.setMemberId(request.getMemberId());
            payment.setAmount(request.getAmount());
            payment.setPaymentMethod(PaymentMethod.POINT);
            payment.setPayType(PayType.PAYMENT);
            payment.setPgCompany(null); // 포인트 결제는 PG사 없음
            payment.setTransactionId(transactionId);
            payment.setApprovalNumber(approvalNumber);
            payment.setPaymentDate(LocalDateTime.now());
            payment.setStatus("SUCCESS");

            // 4. DB 저장
            paymentMapper.insert(payment);

            log.info("POINT payment processed successfully: paymentId={}, transactionId={}, memberId={}",
                    payment.getPaymentId(), payment.getTransactionId(), request.getMemberId());

            return payment;

        } catch (Exception e) {
            log.error("POINT payment processing failed: orderId={}", request.getOrderId(), e);
            throw new RuntimeException("POINT payment processing failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void processRefund(Payment payment) {
        log.info("Processing POINT refund: paymentId={}, orderId={}, amount={}",
                payment.getPaymentId(), payment.getOrderId(), payment.getAmount());

        try {
            // 1. 요청 로그 기록
            PaymentInterfaceRequestLog requestLog = new PaymentInterfaceRequestLog();
            requestLog.setPaymentId(payment.getPaymentId());
            requestLog.setRequestType("REFUND");
            requestLog.setRequestPayload("POINT refund for orderId: " + payment.getOrderId());
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
            String cancelTransactionId = "POINT_CANCEL_" + System.currentTimeMillis();
            String cancelApprovalNumber = "CANCEL_APPROVAL_" + System.currentTimeMillis();

            // 3. 응답 로그 기록
            requestLog.setResponsePayload("Cancel transactionId: " + cancelTransactionId);
            logMapper.updateResponse(requestLog);

            // 4. 환불 Payment 엔티티 생성 (payType=REFUND, 음수 금액)
            // TODO: PaymentMapper.getNextPaymentSequence() 구현 후 시퀀스 기반 ID로 변경
            String refundPaymentId = generatePaymentId();

            Payment refundPayment = new Payment();
            refundPayment.setPaymentId(refundPaymentId);
            refundPayment.setOrderId(payment.getOrderId());
            refundPayment.setMemberId(payment.getMemberId());
            refundPayment.setAmount(payment.getAmount().negate()); // 음수 금액
            refundPayment.setPaymentMethod(PaymentMethod.POINT);
            refundPayment.setPayType(PayType.REFUND);
            refundPayment.setPgCompany(null); // 포인트 결제는 PG사 없음
            refundPayment.setTransactionId(cancelTransactionId);
            refundPayment.setApprovalNumber(cancelApprovalNumber);
            refundPayment.setPaymentDate(LocalDateTime.now());
            refundPayment.setStatus("SUCCESS");
            refundPayment.setClaimId(payment.getClaimId());

            // 5. DB 저장
            paymentMapper.insert(refundPayment);

            log.info("POINT refund processed successfully: originalPaymentId={}, refundPaymentId={}, refundTransactionId={}",
                    payment.getPaymentId(), refundPayment.getPaymentId(), refundPayment.getTransactionId());

        } catch (Exception e) {
            log.error("POINT refund processing failed: paymentId={}", payment.getPaymentId(), e);
            throw new RuntimeException("POINT refund processing failed: " + e.getMessage(), e);
        }
    }

    /**
     * 결제 ID 생성
     * 형식: PAY + YYYYMMDD + 밀리초 타임스탬프
     * TODO: PaymentMapper.getNextPaymentSequence() 구현 후 시퀀스 기반 ID로 변경
     */
    private String generatePaymentId() {
        String dateStr = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String timestampStr = String.valueOf(System.currentTimeMillis() % 10000000000L);
        return "PAY" + dateStr + timestampStr;
    }

    /**
     * 포인트 결제 요청 로그 기록
     */
    private void logPaymentRequest(PaymentConfirmRequest request, String paymentId) {
        try {
            PaymentInterfaceRequestLog requestLog = new PaymentInterfaceRequestLog();
            requestLog.setPaymentId(paymentId);
            requestLog.setRequestType("PAYMENT");
            requestLog.setRequestPayload(objectMapper.writeValueAsString(request));
            requestLog.setTimestamp(LocalDateTime.now());
            logMapper.insert(requestLog);
        } catch (Exception e) {
            log.warn("Failed to log payment request: {}", e.getMessage());
        }
    }

    /**
     * 포인트 결제 응답 로그 기록
     */
    private void logPaymentResponse(String paymentId, String transactionId,
                                      String approvalNumber, String amount) {
        try {
            PaymentInterfaceRequestLog requestLog = new PaymentInterfaceRequestLog();
            requestLog.setPaymentId(paymentId);
            requestLog.setResponsePayload("transactionId: " + transactionId +
                                         ", approvalNumber: " + approvalNumber +
                                         ", amount: " + amount);
            logMapper.updateResponse(requestLog);
        } catch (Exception e) {
            log.warn("Failed to log payment response: {}", e.getMessage());
        }
    }
}
