package com.vibe.pay.backend.payment.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibe.pay.backend.payment.Payment;
import com.vibe.pay.backend.payment.PaymentMapper;
import com.vibe.pay.backend.payment.dto.*;
import com.vibe.pay.backend.paymentlog.PaymentInterfaceRequestLogService;
import com.vibe.pay.backend.util.HashUtils;
import com.vibe.pay.backend.util.WebClientUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * NicePay 결제 게이트웨이 어댑터
 *
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NicePayAdapter implements PaymentGateway {

    private final PaymentMapper paymentMapper;
    private final PaymentInterfaceRequestLogService logService;
    private final WebClientUtil webClientUtil;
    private final ObjectMapper objectMapper;

    @Value("${nicepay.mid:nicepay00m}")
    private String mid;

    @Value("${nicepay.merchantKey:EYzu8jGGMfqaDEp76gSckuvnaHHu+bC4opsSN6lHv3b2lurNYkVXrZ7Z1AoqQnXI3eLuaUFyoRNC6FkrzVjceg==}")
    private String merchantKey;

    @Value("${nicepay.returnUrl:http://localhost:3000/order/progress-popup}")
    private String returnUrl;

    @Value("${nicepay.cancelUrl:http://localhost:3000/order/popup}")
    private String cancelUrl;

    @Override
    public PaymentInitResponse initiate(PaymentInitRequest request) {
        log.debug("NicePay 결제 초기화: orderId={}, amount={}", request.getOrderId(), request.getAmount());

        try {
            // 결제 ID 생성
            String paymentId = generatePaymentId();

            // 타임스탬프 생성
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

            // 응답 생성
            PaymentInitResponse response = PaymentInitResponse.builder()
                    .success(true)
                    .paymentId(paymentId)
                    .selectedPgCompany("NICEPAY")
                    .mid(mid)
                    .merchantKey(merchantKey)
                    .goodsName(request.getGoodName())
                    .amt(String.valueOf(request.getAmount().intValue()))
                    .moid(request.getOrderId())
                    .buyerName(request.getBuyerName())
                    .buyerTel(request.getBuyerTel())
                    .buyerEmail(request.getBuyerEmail())
                    .returnUrl(returnUrl)
                    .cancelUrl(cancelUrl)
                    .build();

            // Payment 엔티티 생성 (초기 상태)
            Payment payment = Payment.builder()
                    .paymentId(paymentId)
                    .memberId(request.getMemberId())
                    .orderId(request.getOrderId())
                    .amount(request.getAmount())
                    .paymentMethod("CREDIT_CARD")
                    .payType("PAYMENT")
                    .pgCompany("NICEPAY")
                    .status("PENDING")
                    .orderStatus("INIT")
                    .paymentDate(LocalDateTime.now())
                    .build();

            paymentMapper.insert(payment);

            log.info("NicePay 결제 초기화 완료: paymentId={}", paymentId);

            return response;

        } catch (Exception e) {
            log.error("NicePay 결제 초기화 실패: orderId={}", request.getOrderId(), e);
            return PaymentInitResponse.builder()
                    .success(false)
                    .errorMessage("NicePay 결제 초기화 실패: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public Payment confirm(PaymentConfirmRequest request) {
        log.debug("NicePay 결제 승인: orderId={}, tid={}", request.getOrderId(), request.getTid());

        try {
            // NicePay 승인 요청
            String ediDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String signData = HashUtils.sha256Hex(request.getTid() + mid + request.getPrice().intValue() + ediDate + merchantKey);

            NicePayConfirmRequest confirmRequest = NicePayConfirmRequest.builder()
                    .tid(request.getTid())
                    .authCode(request.getAuthCode())
                    .mid(mid)
                    .amt(String.valueOf(request.getPrice().intValue()))
                    .ediDate(ediDate)
                    .signData(signData)
                    .build();

            String requestJson = objectMapper.writeValueAsString(confirmRequest);
            log.debug("NicePay 승인 요청: {}", requestJson);

            // PG 승인 API 호출 (실제 환경에서는 NicePay API URL 사용)
            String apiUrl = "https://webapi.nicepay.co.kr/webapi/pay_process.jsp";
            String responseJson = webClientUtil.post(apiUrl, confirmRequest);
            log.debug("NicePay 승인 응답: {}", responseJson);

            // 응답 파싱
            JsonNode responseNode = objectMapper.readTree(responseJson);
            String resultCode = responseNode.path("resultCode").asText();
            String resultMsg = responseNode.path("resultMsg").asText();

            // 로그 저장
            String paymentId = generatePaymentId();
            logService.logRequest(paymentId, "NICEPAY_CONFIRM", requestJson, responseJson);

            if (!"0000".equals(resultCode)) {
                throw new RuntimeException("NicePay 승인 실패: " + resultMsg);
            }

            // Payment 엔티티 생성
            Payment payment = Payment.builder()
                    .paymentId(paymentId)
                    .memberId(request.getMemberId())
                    .orderId(request.getOrderId())
                    .amount(request.getPrice())
                    .paymentMethod("CREDIT_CARD")
                    .payType("PAYMENT")
                    .pgCompany("NICEPAY")
                    .status("APPROVED")
                    .orderStatus("ORDER")
                    .transactionId(request.getTid())
                    .paymentDate(LocalDateTime.now())
                    .build();

            paymentMapper.insert(payment);

            log.info("NicePay 결제 승인 완료: paymentId={}, tid={}", paymentId, payment.getTransactionId());

            return payment;

        } catch (Exception e) {
            log.error("NicePay 결제 승인 실패: orderId={}", request.getOrderId(), e);
            throw new RuntimeException("NicePay 결제 승인 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public Payment cancel(PaymentCancelRequest request) {
        log.debug("NicePay 결제 취소: paymentId={}, transactionId={}", request.getPaymentId(), request.getTransactionId());

        try {
            // 취소 요청 데이터 생성
            String requestData = String.format("{\"tid\":\"%s\",\"cancelAmt\":%d,\"cancelMsg\":\"%s\"}",
                    request.getTransactionId(),
                    request.getAmount().intValue(),
                    request.getReason() != null ? request.getReason() : "고객 요청");

            log.debug("NicePay 취소 요청: {}", requestData);

            // PG 취소 API 호출
            String apiUrl = "https://webapi.nicepay.co.kr/webapi/cancel_process.jsp";
            String responseJson = webClientUtil.post(apiUrl, requestData);
            log.debug("NicePay 취소 응답: {}", responseJson);

            // 로그 저장
            logService.logRequest(request.getPaymentId(), "NICEPAY_CANCEL", requestData, responseJson);

            // Payment 상태 업데이트
            Payment payment = paymentMapper.selectByPaymentId(request.getPaymentId());
            payment.setStatus("CANCELLED");
            payment.setOrderStatus("CANCEL");
            paymentMapper.update(payment);

            log.info("NicePay 결제 취소 완료: paymentId={}", request.getPaymentId());

            return payment;

        } catch (Exception e) {
            log.error("NicePay 결제 취소 실패: paymentId={}", request.getPaymentId(), e);
            throw new RuntimeException("NicePay 결제 취소 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public Payment netCancel(PaymentNetCancelRequest request) {
        log.debug("NicePay 망취소: paymentId={}, transactionId={}", request.getPaymentId(), request.getTransactionId());

        try {
            // 망취소 요청 데이터 생성
            String requestData = String.format("{\"tid\":\"%s\",\"cancelAmt\":%d,\"cancelMsg\":\"주문 생성 실패 - 망취소\"}",
                    request.getTransactionId(),
                    request.getAmount().intValue());

            log.debug("NicePay 망취소 요청: {}", requestData);

            // PG 망취소 API 호출
            String apiUrl = "https://webapi.nicepay.co.kr/webapi/cancel_process.jsp";
            String responseJson = webClientUtil.post(apiUrl, requestData);
            log.debug("NicePay 망취소 응답: {}", responseJson);

            // 로그 저장
            logService.logRequest(request.getPaymentId(), "NICEPAY_NET_CANCEL", requestData, responseJson);

            // Payment 상태 업데이트
            Payment payment = paymentMapper.selectByPaymentId(request.getPaymentId());
            payment.setStatus("NET_CANCELLED");
            payment.setOrderStatus("NET_CANCEL");
            paymentMapper.update(payment);

            log.info("NicePay 망취소 완료: paymentId={}", request.getPaymentId());

            return payment;

        } catch (Exception e) {
            log.error("NicePay 망취소 실패: paymentId={}", request.getPaymentId(), e);
            throw new RuntimeException("NicePay 망취소 실패: " + e.getMessage(), e);
        }
    }

    private String generatePaymentId() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String seq = String.format("%09d", System.currentTimeMillis() % 1000000000);
        return date + "P" + seq;
    }
}
