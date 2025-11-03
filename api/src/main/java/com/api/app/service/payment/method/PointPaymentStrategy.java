package com.api.app.service.payment.method;

import com.api.app.dto.request.order.PayRequest;
import com.api.app.dto.request.point.PointTransactionRequest;
import com.api.app.emum.MEM002;
import com.api.app.emum.MEM003;
import com.api.app.emum.PAY001;
import com.api.app.emum.PAY002;
import com.api.app.emum.PAY003;
import com.api.app.emum.PAY004;
import com.api.app.entity.PayBase;
import com.api.app.entity.PayInterfaceLog;
import com.api.app.repository.pay.PayBaseTrxMapper;
import com.api.app.repository.pay.PayInterfaceLogTrxMapper;
import com.api.app.service.point.PointService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 포인트 결제 전략 구현
 *
 * @author Claude
 * @version 1.0
 * @since 2025-10-31
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PointPaymentStrategy implements PaymentMethodStrategy {

    private final PointService pointService;
    private final PayBaseTrxMapper payBaseTrxMapper;
    private final PayInterfaceLogTrxMapper payInterfaceLogTrxMapper;
    private final ObjectMapper objectMapper;

    @Override
    public PayBase processPayment(String memberNo, String orderNo, PayRequest payRequest) {
        log.info("Point payment processing started. memberNo={}, orderNo={}, amount={}",
                memberNo, orderNo, payRequest.getAmount());

        // 결제번호 생성
        String payNo = payBaseTrxMapper.generatePayNo();

        // 포인트 사용 요청 생성
        PointTransactionRequest pointRequest = new PointTransactionRequest();
        pointRequest.setAmount(payRequest.getAmount());
        pointRequest.setPointTransactionCode(MEM002.USE.getCode()); // 사용
        pointRequest.setPointTransactionReasonCode(MEM003.ORDER.getCode()); // 주문
        pointRequest.setPointTransactionReasonNo(payNo); // 결제번호를 사용

        // 결제 요청 로그 저장 (PAY_004.001: 결제)
        try {
            String requestJson = objectMapper.writeValueAsString(pointRequest);
            createPayInterfaceLog(payNo, memberNo, PAY004.PAYMENT.getCode(), requestJson, null);
        } catch (Exception e) {
            log.error("Failed to log point payment request. payNo={}", payNo, e);
        }

        // 포인트 사용 처리 (트랜잭션으로 묶여있어 실패시 롤백됨)
        pointService.processPointTransaction(memberNo, pointRequest);

        // 승인 결과 로그 저장 (PAY_004.002: 승인) - 포인트는 요청과 승인이 동시 완료
        try {
            String responseJson = String.format("{\"payNo\":\"%s\",\"amount\":%d,\"status\":\"completed\"}",
                    payNo, payRequest.getAmount());
            createPayInterfaceLog(payNo, memberNo, PAY004.APPROVAL.getCode(), null, responseJson);
        } catch (Exception e) {
            log.error("Failed to log point payment approval. payNo={}", payNo, e);
        }

        // PayBase 엔티티 생성
        PayBase payBase = new PayBase();
        payBase.setPayNo(payNo);
        payBase.setPayTypeCode(PAY001.PAYMENT.getCode());
        payBase.setPayWayCode(PAY002.POINT.getCode());
        payBase.setPayStatusCode(PAY003.PAYMENT_COMPLETED.getCode());
        payBase.setOrderNo(orderNo);
        payBase.setPayFinishDateTime(LocalDateTime.now());
        payBase.setMemberNo(memberNo);
        payBase.setAmount(payRequest.getAmount());
        payBase.setCancelableAmount(payRequest.getAmount());

        // PayBase 저장
        int result = payBaseTrxMapper.insertPayBase(payBase);
        if (result != 1) {
            throw new RuntimeException("포인트 결제 정보 저장에 실패했습니다");
        }

        log.info("Point payment completed. memberNo={}, orderNo={}, payNo={}", memberNo, orderNo, payNo);
        return payBase;
    }

    @Override
    public String getPayWayCode() {
        return PAY002.POINT.getCode();
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
