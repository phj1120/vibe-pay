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
import java.security.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 이니시스 PG 전략 구현
 *
 * @author Claude
 * @version 1.0
 * @since 2025-10-31
 */
@Slf4j
@Component
public class InicisPaymentStrategy implements PaymentGatewayStrategy {

    @Value("${payment.inicis.mid}")
    private String mid;

    @Value("${payment.inicis.sign-key}")
    private String signKey;

    @Value("${payment.inicis.return-url}")
    private String returnUrl;

    @Value("${payment.inicis.close-url}")
    private String closeUrl;

    @Value("${payment.inicis.gopaymethod:Card}")
    private String gopaymethod;

    @Value("${payment.inicis.acceptmethod:below1000}")
    private String acceptmethod;

    @Override
    public PaymentInitiateResponse initiatePayment(PaymentInitiateRequest request) {
        log.info("Inicis payment initiate started. orderNumber={}", request.getOrderNumber());

        String timestamp = String.valueOf(System.currentTimeMillis());

        String mKey = sha256Hash(signKey);

        Map<String, String> formData = new HashMap<>();
        formData.put("mid", mid);
        formData.put("oid", request.getOrderNumber());
        formData.put("price", String.valueOf(request.getAmount()));
        formData.put("timestamp", timestamp);
        formData.put("mKey", mKey);
        formData.put("version", "1.0");
        formData.put("currency", "WON");
        formData.put("goodname", request.getProductName());
        formData.put("buyername", request.getBuyerName());
        formData.put("buyertel", request.getBuyerTel());
        formData.put("buyeremail", request.getBuyerEmail());
        formData.put("returnUrl", returnUrl);
        formData.put("closeUrl", closeUrl);
        formData.put("gopaymethod", gopaymethod);
        formData.put("acceptmethod", acceptmethod);
        formData.put("charset", "UTF-8");
        formData.put("use_chkfake", "");

        // signature: oid + price + timestamp
        String signatureData = String.format("oid=%s&price=%s&timestamp=%s",
                request.getOrderNumber(), request.getAmount(), timestamp);
        formData.put("signature", sha256Hash(signatureData));
        // verification: oid + price + signKey + timestamp
        String verificationData = String.format("oid=%s&price=%s&signKey=%s&timestamp=%s",
                request.getOrderNumber(), request.getAmount(), signKey, timestamp);
        formData.put("verification", sha256Hash(verificationData));

        log.info("Inicis payment initiate completed. orderNumber={}", request.getOrderNumber());

        return PaymentInitiateResponse.builder()
                .pgType("INICIS")
                .pgTypeCode("001") // PAY005.INICIS
                .paymentMethod(request.getPaymentMethod())
                .merchantId(mid)
                .merchantKey(signKey)
                .returnUrl(returnUrl)
                .formData(formData)
                .build();
    }

    @Override
    public com.api.app.dto.response.payment.PaymentApprovalResponse approvePayment(
            com.api.app.dto.request.payment.PaymentConfirmRequest request) {
        log.info("Inicis payment approval started. orderNo={}", request.getOrderNo());

        // TODO: 실제 PG사 승인 API 호출 구현
        // POST {authUrl}
        // Request: mid, authToken, timestamp, signature, verification, charset, format, price
        // Response: resultCode, resultMsg, tid, mid, MOID, TotPrice, goodName, payMethod,
        //           applDate, applTime, applNum, CARD_Num, CARD_Code

        log.warn("Inicis payment approval not implemented yet. TODO: call PG approval API");

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
        log.info("Inicis payment cancellation started. orderNo={}", request.getOrderNo());

        // TODO: 실제 PG사 망취소 API 호출 구현
        // POST {netCancelUrl}

        log.warn("Inicis payment cancellation not implemented yet. TODO: call PG netCancel API");
    }

    @Override
    public PAY005 getPgType() {
        return PAY005.INICIS;
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
