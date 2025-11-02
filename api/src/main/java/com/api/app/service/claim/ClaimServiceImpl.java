package com.api.app.service.claim;

import com.api.app.dto.request.claim.CancelRequest;
import com.api.app.dto.request.claim.ClaimTargetRequest;
import com.api.app.emum.*;
import com.api.app.entity.OrderDetail;
import com.api.app.entity.OrderGoods;
import com.api.app.entity.PayBase;
import com.api.app.entity.PayInterfaceLog;
import com.api.app.dto.request.payment.PaymentCancelRequest;
import com.api.app.service.payment.strategy.PaymentGatewayFactory;
import com.api.app.service.payment.strategy.PaymentGatewayStrategy;
import com.api.app.repository.order.*;
import com.api.app.repository.pay.PayBaseMapper;
import com.api.app.repository.pay.PayBaseTrxMapper;
import com.api.app.repository.pay.PayInterfaceLogTrxMapper;
import com.api.app.repository.member.MemberBaseMapper;
import com.api.app.service.point.PointService;
import com.api.app.dto.request.point.PointTransactionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 클레임 서비스 구현
 *
 * @author Claude
 * @version 1.0
 * @since 2025-11-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimServiceImpl implements ClaimService {

    private final OrderBaseTrxMapper orderBaseTrxMapper;
    private final OrderDetailMapper orderDetailMapper;
    private final OrderDetailTrxMapper orderDetailTrxMapper;
    private final OrderGoodsMapper orderGoodsMapper;
    private final PayBaseMapper payBaseMapper;
    private final PayBaseTrxMapper payBaseTrxMapper;
    private final PayInterfaceLogTrxMapper payInterfaceLogTrxMapper;
    private final MemberBaseMapper memberBaseMapper;
    private final PointService pointService;
    private final PaymentGatewayFactory paymentGatewayFactory;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void cancelOrder(CancelRequest request) {
        log.info("Order cancel started. memberNo={}, targetCount={}",
                request.getMemberNo(), request.getTargets().size());

        try {
            // 1. 클레임번호 생성
            String claimNo = orderBaseTrxMapper.generateClaimNo();
            log.info("Claim number generated: {}", claimNo);

            // 2. 주문번호별로 그룹핑
            Map<String, List<ClaimTargetRequest>> targetsByOrderNo = request.getTargets().stream()
                    .collect(Collectors.groupingBy(ClaimTargetRequest::getOrderNo));

            // 3. 주문번호별로 처리
            for (Map.Entry<String, List<ClaimTargetRequest>> entry : targetsByOrderNo.entrySet()) {
                String orderNo = entry.getKey();
                List<ClaimTargetRequest> targets = entry.getValue();

                log.info("Processing cancel for orderNo={}, claimNo={}, targetCount={}",
                        orderNo, claimNo, targets.size());

                // 3-1. 취소 대상 검증
                validateCancelTargets(orderNo, targets, request.getMemberNo());

                // 3-2. 취소 금액 계산
                Map<String, Long> cancelAmounts = calculateCancelAmounts(orderNo, targets);

                // 3-3. 결제 취소 처리 (우선순위 역순)
                processCancelPayments(orderNo, claimNo, cancelAmounts, request.getMemberNo());

                // 3-4. 주문 상세 생성 (클레임 데이터)
                createCancelOrderDetails(orderNo, claimNo, targets, request.getMemberNo());
            }

            log.info("Order cancel completed successfully. memberNo={}", request.getMemberNo());

        } catch (Exception e) {
            log.error("Order cancel failed. memberNo={}", request.getMemberNo(), e);
            throw e;
        }
    }

    /**
     * 취소 대상 검증
     */
    private void validateCancelTargets(String orderNo, List<ClaimTargetRequest> targets, String memberNo) {
        for (ClaimTargetRequest target : targets) {
            // 주문 상세 조회
            OrderDetail orderDetail = orderDetailMapper.selectOrderDetailByKey(
                    target.getOrderNo(),
                    target.getOrderSequence(),
                    target.getOrderProcessSequence()
            );

            if (orderDetail == null) {
                throw new IllegalArgumentException("주문 정보를 찾을 수 없습니다");
            }

            // 주문 유형이 '주문'인지 확인
            if (!ORD001.ORDER.getCode().equals(orderDetail.getOrderTypeCode())) {
                throw new IllegalArgumentException("주문 건만 취소할 수 있습니다");
            }

            // 주문 상태가 '주문접수'인지 확인
            if (!ORD002.ORDER_RECEIVED.getCode().equals(orderDetail.getOrderStatusCode())) {
                throw new IllegalArgumentException("주문접수 상태의 주문만 취소할 수 있습니다");
            }
        }
    }

    /**
     * 취소 금액 계산
     */
    private Map<String, Long> calculateCancelAmounts(String orderNo, List<ClaimTargetRequest> targets) {
        Long totalCancelAmount = 0L;

        // 취소 대상 상품들의 금액 합계 계산
        for (ClaimTargetRequest target : targets) {
            OrderDetail orderDetail = orderDetailMapper.selectOrderDetailByKey(
                    target.getOrderNo(),
                    target.getOrderSequence(),
                    target.getOrderProcessSequence()
            );

            OrderGoods orderGoods = orderGoodsMapper.selectOrderGoodsByKey(
                    target.getOrderNo(),
                    orderDetail.getGoodsNo(),
                    orderDetail.getItemNo()
            );

            Long itemAmount = orderGoods.getSalePrice() * orderDetail.getQuantity();
            totalCancelAmount += itemAmount;
        }

        // 원결제 정보 조회 (payTypeCode = 001: 결제, payWayCode별로 조회)
        List<PayBase> originalPayments = payBaseMapper.selectPayBaseByOrderNo(orderNo);
        originalPayments = originalPayments.stream()
                .filter(p -> PAY004.PAYMENT.getCode().equals(p.getPayTypeCode()) ||
                             PAY004.APPROVAL.getCode().equals(p.getPayTypeCode()))
                .sorted(Comparator.comparingInt(p ->
                        PAY002.findByCode(p.getPayWayCode()).getDisplaySequence()))
                .collect(Collectors.toList());

        // 결제 방식별 취소 금액 계산 (우선순위대로 차감)
        Map<String, Long> cancelAmounts = new HashMap<>();
        Long remainingAmount = totalCancelAmount;

        for (PayBase payment : originalPayments) {
            if (remainingAmount <= 0) {
                break;
            }

            Long cancelableAmount = payment.getCancelableAmount();
            if (cancelableAmount == null || cancelableAmount <= 0) {
                continue;
            }

            Long cancelAmount = Math.min(remainingAmount, cancelableAmount);
            cancelAmounts.put(payment.getPayNo(), cancelAmount);
            remainingAmount -= cancelAmount;

            log.info("Cancel amount calculated. payNo={}, payWayCode={}, cancelAmount={}",
                    payment.getPayNo(), payment.getPayWayCode(), cancelAmount);
        }

        if (remainingAmount > 0) {
            throw new IllegalStateException("취소 가능한 금액이 부족합니다");
        }

        return cancelAmounts;
    }

    /**
     * 결제 취소 처리 (우선순위 역순)
     */
    private void processCancelPayments(String orderNo, String claimNo,
                                        Map<String, Long> cancelAmounts, String memberNo) {
        LocalDateTime now = LocalDateTime.now();

        // 취소할 결제들을 역순으로 정렬 (포인트 먼저, 카드 나중)
        List<PayBase> paymentsToCancel = payBaseMapper.selectPayBaseByOrderNo(orderNo).stream()
                .filter(p -> cancelAmounts.containsKey(p.getPayNo()))
                .sorted(Comparator.comparingInt(p ->
                        -PAY002.findByCode(p.getPayWayCode()).getDisplaySequence()))
                .collect(Collectors.toList());

        for (PayBase originalPayment : paymentsToCancel) {
            Long cancelAmount = cancelAmounts.get(originalPayment.getPayNo());

            // 결제 취소에 따른 전략 실행
            if (PAY002.CREDIT_CARD.getCode().equals(originalPayment.getPayWayCode())) {
                // 카드 결제 취소
                processCreditCardCancel(originalPayment, cancelAmount, claimNo, memberNo, now);
            } else if (PAY002.POINT.getCode().equals(originalPayment.getPayWayCode())) {
                // 포인트 결제 취소
                processPointCancel(originalPayment, cancelAmount, claimNo, memberNo, now);
            }

            // 원 결제의 취소가능금액 차감
            updateCancelableAmount(originalPayment.getPayNo(), cancelAmount);

            log.info("Payment cancelled successfully. payNo={}, payWayCode={}, cancelAmount={}",
                    originalPayment.getPayNo(), originalPayment.getPayWayCode(), cancelAmount);
        }
    }

    /**
     * 카드 결제 취소
     */
    private void processCreditCardCancel(PayBase originalPayment, Long cancelAmount,
                                          String claimNo, String memberNo, LocalDateTime now) {
        log.info("Credit card cancel initiated. payNo={}, pgTypeCode={}, cancelAmount={}",
                originalPayment.getPayNo(), originalPayment.getPgTypeCode(), cancelAmount);

        // 결제번호 생성
        String payNo = payBaseTrxMapper.generatePayNo();

        try {
            // PG사 취소 API 호출
            PAY005 pgType = PAY005.findByCode(originalPayment.getPgTypeCode());
            PaymentGatewayStrategy strategy = paymentGatewayFactory.getStrategy(pgType);

            PaymentCancelRequest cancelRequest = PaymentCancelRequest.builder()
                    .pgTypeCode(originalPayment.getPgTypeCode())
                    .transactionId(originalPayment.getTrdNo())
                    .orderNo(originalPayment.getOrderNo())
                    .cancelAmount(cancelAmount)
                    .cancelReason("주문 취소")
                    .partialCancelCode("0") // 전체 취소
                    .build();

            // PG사 취소 API 호출 및 로그 저장
            String requestJson = objectMapper.writeValueAsString(cancelRequest);
            String responseJson = null;

            try {
                strategy.cancelPaymentByOrder(cancelRequest);
                responseJson = "{\"success\": true}";
            } catch (Exception e) {
                responseJson = "{\"success\": false, \"error\": \"" + e.getMessage() + "\"}";
                throw e;
            } finally {
                // pay_interface_log 생성
                // TODO: PAY001 enum에 CANCEL 값 추가 필요 (현재는 "004" 하드코딩)
                createPayInterfaceLog(payNo, memberNo, "004", requestJson, responseJson);
            }

            // PayBase 생성 (취소)
            PayBase cancelPayment = new PayBase();
            cancelPayment.setPayNo(payNo);
            cancelPayment.setPayTypeCode(PAY004.CANCEL.getCode());
            cancelPayment.setPayWayCode(originalPayment.getPayWayCode());
            cancelPayment.setPayStatusCode(PAY003.PAYMENT_CANCELLED.getCode());
            cancelPayment.setOrderNo(originalPayment.getOrderNo());
            cancelPayment.setClaimNo(claimNo);
            cancelPayment.setUpperPayNo(originalPayment.getPayNo());
            cancelPayment.setPayFinishDateTime(now);
            cancelPayment.setMemberNo(memberNo);
            cancelPayment.setAmount(cancelAmount);
            cancelPayment.setCancelableAmount(0L); // 취소 건은 재취소 불가
            cancelPayment.setPgTypeCode(originalPayment.getPgTypeCode());
            cancelPayment.setTrdNo(originalPayment.getTrdNo()); // 원 거래번호 유지
            cancelPayment.setRegistId(memberNo);
            cancelPayment.setRegistDateTime(now);
            cancelPayment.setModifyId(memberNo);
            cancelPayment.setModifyDateTime(now);

            int result = payBaseTrxMapper.insertPayBase(cancelPayment);
            if (result != 1) {
                throw new RuntimeException("결제 취소 정보 저장에 실패했습니다");
            }

            log.info("Credit card cancel payment created. payNo={}, cancelAmount={}", payNo, cancelAmount);

        } catch (Exception e) {
            log.error("Credit card cancel failed. payNo={}", originalPayment.getPayNo(), e);
            throw new RuntimeException("카드 결제 취소에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 포인트 결제 취소
     */
    private void processPointCancel(PayBase originalPayment, Long cancelAmount,
                                      String claimNo, String memberNo, LocalDateTime now) {
        log.info("Point cancel initiated. payNo={}, cancelAmount={}",
                originalPayment.getPayNo(), cancelAmount);

        // 결제번호 생성
        String payNo = payBaseTrxMapper.generatePayNo();

        try {
            // 포인트 취소 API 호출 (/api/point/transaction)
            // 포인트 취소 = 포인트 적립 (원래 사용한 포인트를 돌려줌)
            PointTransactionRequest pointRequest = new PointTransactionRequest();
            pointRequest.setAmount(cancelAmount);
            pointRequest.setPointTransactionCode(MEM002.EARN.getCode()); // 001: 적립 (취소 = 적립)
            pointRequest.setPointTransactionReasonCode(MEM003.ORDER.getCode()); // 002: 주문 (주문 관련 포인트 처리)
            pointRequest.setPointTransactionReasonNo(payNo); // 취소 결제번호

            pointService.processPointTransaction(memberNo, pointRequest);

            log.info("Point cancel transaction completed. payNo={}, cancelAmount={}", payNo, cancelAmount);

            // PayBase 생성 (취소)
            PayBase cancelPayment = new PayBase();
            cancelPayment.setPayNo(payNo);
            cancelPayment.setPayTypeCode(PAY004.CANCEL.getCode());
            cancelPayment.setPayWayCode(originalPayment.getPayWayCode());
            cancelPayment.setPayStatusCode(PAY003.PAYMENT_CANCELLED.getCode());
            cancelPayment.setOrderNo(originalPayment.getOrderNo());
            cancelPayment.setClaimNo(claimNo);
            cancelPayment.setUpperPayNo(originalPayment.getPayNo());
            cancelPayment.setPayFinishDateTime(now);
            cancelPayment.setMemberNo(memberNo);
            cancelPayment.setAmount(cancelAmount);
            cancelPayment.setCancelableAmount(0L); // 취소 건은 재취소 불가
            cancelPayment.setRegistId(memberNo);
            cancelPayment.setRegistDateTime(now);
            cancelPayment.setModifyId(memberNo);
            cancelPayment.setModifyDateTime(now);

            int result = payBaseTrxMapper.insertPayBase(cancelPayment);
            if (result != 1) {
                throw new RuntimeException("결제 취소 정보 저장에 실패했습니다");
            }

            log.info("Point cancel payment created. payNo={}, cancelAmount={}", payNo, cancelAmount);

        } catch (Exception e) {
            log.error("Point cancel failed. payNo={}", originalPayment.getPayNo(), e);
            throw new RuntimeException("포인트 결제 취소에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 원 결제의 취소가능금액 차감
     */
    private void updateCancelableAmount(String payNo, Long cancelAmount) {
        int result = payBaseTrxMapper.updateCancelableAmount(payNo, cancelAmount);
        if (result != 1) {
            throw new RuntimeException("취소가능금액 갱신에 실패했습니다");
        }
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
            payInterfaceLog.setRegistId(memberNo);
            payInterfaceLog.setRegistDateTime(LocalDateTime.now());
            payInterfaceLog.setModifyId(memberNo);
            payInterfaceLog.setModifyDateTime(LocalDateTime.now());

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

    /**
     * 주문 상세 생성 (클레임 데이터)
     */
    private void createCancelOrderDetails(String orderNo, String claimNo,
                                           List<ClaimTargetRequest> targets, String memberNo) {
        LocalDateTime now = LocalDateTime.now();

        for (ClaimTargetRequest target : targets) {
            // 원건 조회
            OrderDetail originalDetail = orderDetailMapper.selectOrderDetailByKey(
                    target.getOrderNo(),
                    target.getOrderSequence(),
                    target.getOrderProcessSequence()
            );

            // 새 OrderDetail 생성 (클레임)
            OrderDetail cancelDetail = new OrderDetail();
            cancelDetail.setOrderNo(orderNo);
            cancelDetail.setOrderSequence(target.getOrderSequence());
            cancelDetail.setOrderProcessSequence(originalDetail.getOrderProcessSequence() + 1);
            cancelDetail.setUpperOrderProcessSequence(target.getOrderProcessSequence());
            cancelDetail.setClaimNo(claimNo);
            cancelDetail.setGoodsNo(originalDetail.getGoodsNo());
            cancelDetail.setItemNo(originalDetail.getItemNo());
            cancelDetail.setQuantity(originalDetail.getQuantity());
            cancelDetail.setOrderStatusCode(ORD002.ORDER_CANCELLED.getCode());
            cancelDetail.setDeliveryTypeCode(DLV001.COLLECTION.getCode()); // 회수 (반품배송)
            cancelDetail.setOrderTypeCode(ORD001.ORDER_CANCEL.getCode());
            cancelDetail.setOrderAcceptDtm(now);
            cancelDetail.setRegistId(memberNo);
            cancelDetail.setRegistDateTime(now);
            cancelDetail.setModifyId(memberNo);
            cancelDetail.setModifyDateTime(now);

            int result = orderDetailTrxMapper.insertOrderDetail(cancelDetail);
            if (result != 1) {
                throw new RuntimeException("주문 취소 상세 정보 저장에 실패했습니다");
            }

            log.info("Cancel order detail created. orderNo={}, orderSequence={}, orderProcessSequence={}",
                    orderNo, cancelDetail.getOrderSequence(), cancelDetail.getOrderProcessSequence());
        }
    }
}
