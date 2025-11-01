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
 * INICIS 결제 게이트웨이 어댑터
 *
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InicisAdapter implements PaymentGateway {

    private final PaymentMapper paymentMapper;
    private final PaymentInterfaceRequestLogService logService;
    private final WebClientUtil webClientUtil;
    private final ObjectMapper objectMapper;

    @Value("${inicis.mid:INIpayTest}")
    private String mid;

    @Value("${inicis.signKey:SU5JTElURV9UUklQTEVERVNfS0VZU1RS}")
    private String signKey;

    @Value("${inicis.apiKey:ItEQKi3rY7uvDS8l}")
    private String apiKey;

    @Value("${inicis.returnUrl:http://localhost:3000/order/progress-popup}")
    private String returnUrl;

    @Value("${inicis.closeUrl:http://localhost:3000/order/popup}")
    private String closeUrl;

    @Value("${inicis.refundUrl:https://iniapi.inicis.com/v2/pg/refund}")
    private String refundUrl;

    @Override
    public PaymentInitResponse initiate(PaymentInitRequest request) {
        log.debug("INICIS 결제 초기화: orderId={}, amount={}", request.getOrderId(), request.getAmount());

        try {
            // 결제 ID 생성
            String paymentId = generatePaymentId();

            // 타임스탬프 생성
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

            // 서명 데이터 생성
            String signData = String.format("oid=%s&price=%s&timestamp=%s",
                    request.getOrderId(),
                    request.getAmount().intValue(),
                    timestamp);
            String signature = HashUtils.generateSignature(signData, signKey);

            log.debug("INICIS 서명 생성: signData={}, signature={}", signData, signature);

            // 응답 생성
            PaymentInitResponse response = PaymentInitResponse.builder()
                    .success(true)
                    .paymentId(paymentId)
                    .selectedPgCompany("INICIS")
                    .mid(mid)
                    .timestamp(timestamp)
                    .oid(request.getOrderId())
                    .price(String.valueOf(request.getAmount().intValue()))
                    .goodName(request.getGoodName())
                    .signature(signature)
                    .returnUrl(returnUrl)
                    .closeUrl(closeUrl)
                    .build();

            // Payment 엔티티 생성 (초기 상태)
            Payment payment = Payment.builder()
                    .paymentId(paymentId)
                    .memberId(request.getMemberId())
                    .orderId(request.getOrderId())
                    .amount(request.getAmount())
                    .paymentMethod("CREDIT_CARD")
                    .payType("PAYMENT")
                    .pgCompany("INICIS")
                    .status("PENDING")
                    .orderStatus("INIT")
                    .paymentDate(LocalDateTime.now())
                    .build();

            paymentMapper.insert(payment);

            log.info("INICIS 결제 초기화 완료: paymentId={}", paymentId);

            return response;

        } catch (Exception e) {
            log.error("INICIS 결제 초기화 실패: orderId={}", request.getOrderId(), e);
            return PaymentInitResponse.builder()
                    .success(false)
                    .errorMessage("INICIS 결제 초기화 실패: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public Payment confirm(PaymentConfirmRequest request) {
        log.debug("INICIS 결제 승인: orderId={}, authToken={}", request.getOrderId(), request.getAuthToken());

        try {
            // INICIS 승인 요청
            InicisConfirmRequest confirmRequest = InicisConfirmRequest.builder()
                    .authToken(request.getAuthToken())
                    .authUrl(request.getAuthUrl())
                    .netCancelUrl(request.getNetCancelUrl())
                    .charset("UTF-8")
                    .format("JSON")
                    .build();

            String requestJson = objectMapper.writeValueAsString(confirmRequest);
            log.debug("INICIS 승인 요청: {}", requestJson);

            // PG 승인 API 호출
            String responseJson = webClientUtil.post(request.getAuthUrl(), confirmRequest);
            log.debug("INICIS 승인 응답: {}", responseJson);

            // 응답 파싱
            JsonNode responseNode = objectMapper.readTree(responseJson);
            String resultCode = responseNode.path("resultCode").asText();
            String resultMsg = responseNode.path("resultMsg").asText();

            // 로그 저장
            String paymentId = generatePaymentId();
            logService.logRequest(paymentId, "INICIS_CONFIRM", requestJson, responseJson);

            if (!"00".equals(resultCode)) {
                throw new RuntimeException("INICIS 승인 실패: " + resultMsg);
            }

            // Payment 엔티티 업데이트
            Payment payment = Payment.builder()
                    .paymentId(paymentId)
                    .memberId(request.getMemberId())
                    .orderId(request.getOrderId())
                    .amount(request.getPrice())
                    .paymentMethod("CREDIT_CARD")
                    .payType("PAYMENT")
                    .pgCompany("INICIS")
                    .status("APPROVED")
                    .orderStatus("ORDER")
                    .transactionId(responseNode.path("tid").asText())
                    .paymentDate(LocalDateTime.now())
                    .build();

            paymentMapper.insert(payment);

            log.info("INICIS 결제 승인 완료: paymentId={}, tid={}", paymentId, payment.getTransactionId());

            return payment;

        } catch (Exception e) {
            log.error("INICIS 결제 승인 실패: orderId={}", request.getOrderId(), e);
            throw new RuntimeException("INICIS 결제 승인 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public Payment cancel(PaymentCancelRequest request) {
        log.debug("INICIS 결제 취소: paymentId={}, transactionId={}", request.getPaymentId(), request.getTransactionId());

        try {
            // 취소 요청 데이터 생성
            String requestData = String.format("{\"tid\":\"%s\",\"msg\":\"%s\"}",
                    request.getTransactionId(),
                    request.getReason() != null ? request.getReason() : "고객 요청");

            log.debug("INICIS 취소 요청: {}", requestData);

            // PG 취소 API 호출
            String responseJson = webClientUtil.post(refundUrl, requestData);
            log.debug("INICIS 취소 응답: {}", responseJson);

            // 로그 저장
            logService.logRequest(request.getPaymentId(), "INICIS_CANCEL", requestData, responseJson);

            // Payment 상태 업데이트
            Payment payment = paymentMapper.selectById(request.getPaymentId());
            payment.setStatus("CANCELLED");
            payment.setOrderStatus("CANCEL");
            paymentMapper.update(payment);

            log.info("INICIS 결제 취소 완료: paymentId={}", request.getPaymentId());

            return payment;

        } catch (Exception e) {
            log.error("INICIS 결제 취소 실패: paymentId={}", request.getPaymentId(), e);
            throw new RuntimeException("INICIS 결제 취소 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public Payment netCancel(PaymentNetCancelRequest request) {
        log.debug("INICIS 망취소: paymentId={}, transactionId={}", request.getPaymentId(), request.getTransactionId());

        try {
            // 망취소 요청 데이터 생성
            String requestData = String.format("{\"tid\":\"%s\",\"msg\":\"주문 생성 실패 - 망취소\"}",
                    request.getTransactionId());

            log.debug("INICIS 망취소 요청: {}", requestData);

            // PG 망취소 API 호출
            String responseJson = webClientUtil.post(request.getNetCancelUrl(), requestData);
            log.debug("INICIS 망취소 응답: {}", responseJson);

            // 로그 저장
            logService.logRequest(request.getPaymentId(), "INICIS_NET_CANCEL", requestData, responseJson);

            // Payment 상태 업데이트
            Payment payment = paymentMapper.selectById(request.getPaymentId());
            payment.setStatus("NET_CANCELLED");
            payment.setOrderStatus("NET_CANCEL");
            paymentMapper.update(payment);

            log.info("INICIS 망취소 완료: paymentId={}", request.getPaymentId());

            return payment;

        } catch (Exception e) {
            log.error("INICIS 망취소 실패: paymentId={}", request.getPaymentId(), e);
            throw new RuntimeException("INICIS 망취소 실패: " + e.getMessage(), e);
        }
    }

    private String generatePaymentId() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String seq = String.format("%09d", System.currentTimeMillis() % 1000000000);
        return date + "P" + seq;
    }
}
