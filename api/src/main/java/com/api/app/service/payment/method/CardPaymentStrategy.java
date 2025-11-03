package com.api.app.service.payment.method;

import com.api.app.dto.request.order.PayRequest;
import com.api.app.dto.response.payment.PaymentApprovalResponse;
import com.api.app.emum.PAY001;
import com.api.app.emum.PAY002;
import com.api.app.emum.PAY003;
import com.api.app.emum.PAY004;
import com.api.app.emum.PAY005;
import com.api.app.entity.PayBase;
import com.api.app.entity.PayInterfaceLog;
import com.api.app.repository.pay.PayBaseTrxMapper;
import com.api.app.repository.pay.PayInterfaceLogTrxMapper;
import com.api.app.service.payment.strategy.PaymentGatewayFactory;
import com.api.app.service.payment.strategy.PaymentGatewayStrategy;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 카드 결제 전략 구현
 *
 * @author Claude
 * @version 1.0
 * @since 2025-10-31
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CardPaymentStrategy implements PaymentMethodStrategy {

    private final PaymentGatewayFactory paymentGatewayFactory;
    private final PayBaseTrxMapper payBaseTrxMapper;
    private final PayInterfaceLogTrxMapper payInterfaceLogTrxMapper;
    private final ObjectMapper objectMapper;

    @Override
    public PayBase processPayment(String memberNo, String orderNo, PayRequest payRequest) {
        log.info("Card payment processing started. orderNo={}, amount={}",
                orderNo, payRequest.getAmount());

        // PG사 선택 및 승인 처리
        PAY005 pgType = PAY005.findByCode(payRequest.getPaymentConfirmRequest().getPgTypeCode());
        PaymentGatewayStrategy pgStrategy = paymentGatewayFactory.getStrategy(pgType);

        // 결제번호 생성 (로그에 사용)
        String payNo = payBaseTrxMapper.generatePayNo();

        // 결제 요청 로그 저장 (PAY_004.001: 결제)
        try {
            String requestJson = objectMapper.writeValueAsString(payRequest.getPaymentConfirmRequest());
            createPayInterfaceLog(payNo, memberNo, PAY004.PAYMENT.getCode(), requestJson, null);
        } catch (Exception e) {
            log.error("Failed to log payment request. payNo={}", payNo, e);
        }

        // 승인 처리
        PaymentApprovalResponse approvalResponse = pgStrategy.approvePayment(
                payRequest.getPaymentConfirmRequest());

        // 승인 결과 로그 저장 (PAY_004.002: 승인)
        try {
            String responseJson = objectMapper.writeValueAsString(approvalResponse);
            createPayInterfaceLog(payNo, memberNo, PAY004.APPROVAL.getCode(), null, responseJson);
        } catch (Exception e) {
            log.error("Failed to log payment approval. payNo={}", payNo, e);
        }

        // PayBase 엔티티 생성
        PayBase payBase = new PayBase();
        payBase.setPayNo(payNo);
        payBase.setPayTypeCode(PAY001.PAYMENT.getCode());
        payBase.setPayWayCode(PAY002.CREDIT_CARD.getCode());
        payBase.setPayStatusCode(PAY003.PAYMENT_COMPLETED.getCode());
        payBase.setApproveNo(approvalResponse.getApproveNo());
        payBase.setOrderNo(orderNo);
        payBase.setTrdNo(approvalResponse.getTrdNo());
        payBase.setPayFinishDateTime(LocalDateTime.now());
        payBase.setMemberNo(memberNo);
        payBase.setAmount(payRequest.getAmount());
        payBase.setCancelableAmount(payRequest.getAmount());
        payBase.setPgTypeCode(payRequest.getPaymentConfirmRequest().getPgTypeCode());

        // PayBase 저장
        int result = payBaseTrxMapper.insertPayBase(payBase);
        if (result != 1) {
            throw new RuntimeException("카드 결제 정보 저장에 실패했습니다");
        }

        log.info("Card payment completed. orderNo={}, payNo={}", orderNo, payNo);
        return payBase;
    }

    @Override
    public String getPayWayCode() {
        return PAY002.CREDIT_CARD.getCode();
    }

    /**
     * pay_interface_log 생성
     */
    private void createPayInterfaceLog(String payNo, String memberNo, String payLogCode,
                                        String requestJson, String responseJson) {
        try {
            String payInterfaceNo = payInterfaceLogTrxMapper.generatePayInterfaceNo();

            PayInterfaceLog payInterfaceLog = new PayInterfaceLog();
            payInterfaceLog.setPayInterfaceNo(payInterfaceNo);
            payInterfaceLog.setMemberNo(memberNo);
            payInterfaceLog.setPayNo(payNo);
            payInterfaceLog.setPayLogCode(payLogCode);
            payInterfaceLog.setRequestJson(requestJson);
            payInterfaceLog.setResponseJson(responseJson);

            int result = payInterfaceLogTrxMapper.insertPayInterfaceLog(payInterfaceLog);
            if (result != 1) {
                log.error("Failed to create pay_interface_log. payNo={}", payNo);
            }

            log.info("Pay interface log created. payInterfaceNo={}, payLogCode={}",
                    payInterfaceNo, payLogCode);

        } catch (Exception e) {
            log.error("Error creating pay_interface_log. payNo={}", payNo, e);
            // 로그 생성 실패는 전체 프로세스를 중단하지 않음
        }
    }
}
