package com.api.app.service.payment.strategy;

import com.api.app.dto.request.payment.PaymentInitiateRequest;
import com.api.app.dto.response.payment.PaymentInitiateResponse;
import com.api.app.emum.PAY005;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 나이스 PG 전략 구현
 *
 * @author Claude
 * @version 1.0
 * @since 2025-10-31
 */
@Slf4j
@Component
public class NicePaymentStrategy implements PaymentGatewayStrategy {

    @Value("${payment.nice.mid}")
    private String mid;

    @Value("${payment.nice.merchant-key}")
    private String merchantKey;

    @Value("${payment.nice.return-url}")
    private String returnUrl;

    @Value("${payment.nice.cancel-url}")
    private String cancelUrl;

    @Override
    public PaymentInitiateResponse initiatePayment(PaymentInitiateRequest request) {
        log.info("Nice payment initiate started. orderNumber={}", request.getOrderNumber());

        String ediDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        Map<String, String> formData = new HashMap<>();
        formData.put("PayMethod", "CARD");
        formData.put("MID", mid);
        formData.put("Moid", request.getOrderNumber());
        formData.put("Amt", String.valueOf(request.getAmount()));
        formData.put("GoodsName", request.getProductName());
        formData.put("BuyerName", request.getBuyerName());
        formData.put("BuyerEmail", request.getBuyerEmail());
        formData.put("BuyerTel", request.getBuyerTel());
        formData.put("ReturnURL", returnUrl);
        formData.put("EdiDate", ediDate);
        formData.put("CharSet", "UTF-8");

        // SignData: EdiDate + MID + Amt + MerchantKey
        String signData = ediDate + mid + request.getAmount() + merchantKey;
        formData.put("SignData", sha256Hash(signData));

        log.info("Nice payment initiate completed. orderNumber={}", request.getOrderNumber());

        return PaymentInitiateResponse.builder()
                .pgType("NICE")
                .pgTypeCode("002") // PAY005.NICE
                .paymentMethod(request.getPaymentMethod())
                .merchantId(mid)
                .merchantKey(merchantKey)
                .returnUrl(returnUrl)
                .formData(formData)
                .build();
    }

    @Override
    public com.api.app.dto.response.payment.PaymentApprovalResponse approvePayment(
            com.api.app.dto.request.payment.PaymentConfirmRequest request) {
        log.info("Nice payment approval started. orderNo={}", request.getOrderNo());

        // TODO: 실제 PG사 승인 API 호출 구현
        // POST {authUrl}
        // Request: TID, AuthToken, MID, Amt, EdiDate, SignData, CharSet, EdiType
        // Response: ResultCode, ResultMsg, Amt, MID, Moid, Signature, TID, AuthCode, CardCode, CardNo

        log.warn("Nice payment approval not implemented yet. TODO: call PG approval API");

        // 임시 응답 반환
        return com.api.app.dto.response.payment.PaymentApprovalResponse.builder()
                .approveNo("TEMP_APPROVE_NO")
                .trdNo("TEMP_TRD_NO")
                .amount(0L)
                .cardNo("****-****-****-****")
                .cardCode("TEMP_CARD_CODE")
                .build();
    }

    @Override
    public void cancelPayment(com.api.app.dto.request.payment.PaymentConfirmRequest request) {
        log.info("Nice payment cancellation started. orderNo={}", request.getOrderNo());

        // TODO: 실제 PG사 망취소 API 호출 구현
        // POST {netCancelUrl}

        log.warn("Nice payment cancellation not implemented yet. TODO: call PG netCancel API");
    }

    @Override
    public PAY005 getPgType() {
        return PAY005.NICE;
    }

    /**
     * SHA-256 해싱
     */
    private String sha256Hash(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(text.getBytes(StandardCharsets.UTF_8));
            return String.format("%064x", new BigInteger(1, md.digest()));
        } catch (Exception e) {
            log.error("SHA-256 hashing failed", e);
            throw new RuntimeException("SHA-256 해싱에 실패했습니다", e);
        }
    }
}
