package com.api.app.service.payment.method;

import com.api.app.dto.request.order.PayRequest;
import com.api.app.dto.response.payment.PaymentApprovalResponse;
import com.api.app.emum.PAY001;
import com.api.app.emum.PAY002;
import com.api.app.emum.PAY003;
import com.api.app.entity.PayBase;
import com.api.app.repository.pay.PayBaseTrxMapper;
import com.api.app.service.payment.strategy.PaymentGatewayFactory;
import com.api.app.service.payment.strategy.PaymentGatewayStrategy;
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

    @Override
    public PayBase processPayment(String memberNo, String orderNo, PayRequest payRequest) {
        log.info("Card payment processing started. orderNo={}, amount={}",
                orderNo, payRequest.getAmount());

        // PG사 선택 및 승인 처리
        PaymentGatewayStrategy pgStrategy = paymentGatewayFactory
                .getStrategy(payRequest.getPaymentConfirmRequest().getPgTypeCode());

        PaymentApprovalResponse approvalResponse = pgStrategy.approvePayment(
                payRequest.getPaymentConfirmRequest());

        // 결제번호 생성
        String payNo = payBaseTrxMapper.generatePayNo();

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
        payBase.setRegistId(memberNo);
        payBase.setRegistDateTime(LocalDateTime.now());
        payBase.setModifyId(memberNo);
        payBase.setModifyDateTime(LocalDateTime.now());

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
}
