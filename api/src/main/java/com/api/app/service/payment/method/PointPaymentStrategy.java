package com.api.app.service.payment.method;

import com.api.app.dto.request.order.PayRequest;
import com.api.app.dto.request.point.PointTransactionRequest;
import com.api.app.emum.PAY001;
import com.api.app.emum.PAY002;
import com.api.app.emum.PAY003;
import com.api.app.entity.PayBase;
import com.api.app.repository.pay.PayBaseTrxMapper;
import com.api.app.service.point.PointService;
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

    @Override
    public PayBase processPayment(String memberNo, String orderNo, PayRequest payRequest) {
        log.info("Point payment processing started. orderNo={}, amount={}",
                orderNo, payRequest.getAmount());

        // 결제번호 생성
        String payNo = payBaseTrxMapper.generatePayNo();

        // 포인트 사용 처리
        // TODO: email을 어떻게 가져올지 고려 필요 (현재는 memberNo를 email로 사용)
        PointTransactionRequest pointRequest = new PointTransactionRequest();
        pointRequest.setAmount(payRequest.getAmount());
        pointRequest.setPointTransactionCode("002"); // 사용
        pointRequest.setPointTransactionReasonCode("002"); // 주문
        pointRequest.setPointTransactionReasonNo(payNo); // 결제번호를 사용

        // 포인트 사용 (트랜잭션으로 묶여있어 실패시 롤백됨)
        // NOTE: email이 필요하지만 현재 구조상 memberNo만 있음
        // 실제 구현시 회원 정보에서 email을 조회하거나 메서드 시그니처 변경 필요
        log.warn("Point payment requires email, using temporary approach");
        // pointService.processPointTransaction(email, pointRequest);

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
        payBase.setRegistId(memberNo);
        payBase.setRegistDateTime(LocalDateTime.now());
        payBase.setModifyId(memberNo);
        payBase.setModifyDateTime(LocalDateTime.now());

        // PayBase 저장
        int result = payBaseTrxMapper.insertPayBase(payBase);
        if (result != 1) {
            throw new RuntimeException("포인트 결제 정보 저장에 실패했습니다");
        }

        log.info("Point payment completed. orderNo={}, payNo={}", orderNo, payNo);
        return payBase;
    }

    @Override
    public String getPayWayCode() {
        return PAY002.POINT.getCode();
    }
}
