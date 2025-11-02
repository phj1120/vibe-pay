package com.api.app.service.order;

import com.api.app.dto.request.order.OrderRequest;
import com.api.app.dto.request.order.PayRequest;
import com.api.app.dto.response.basket.BasketResponse;
import com.api.app.dto.response.order.OrderCompleteResponse;
import com.api.app.dto.response.order.OrderListResponse;
import com.api.app.emum.MEM001;
import com.api.app.emum.ORD001;
import com.api.app.emum.ORD002;
import com.api.app.emum.PAY002;
import com.api.app.entity.*;
import com.api.app.repository.basket.BasketBaseTrxMapper;
import com.api.app.repository.goods.GoodsItemMapper;
import com.api.app.repository.goods.GoodsPriceHistMapper;
import com.api.app.repository.member.MemberBaseMapper;
import com.api.app.repository.order.OrderBaseMapper;
import com.api.app.repository.order.OrderBaseTrxMapper;
import com.api.app.repository.order.OrderDetailTrxMapper;
import com.api.app.repository.order.OrderGoodsMapper;
import com.api.app.repository.order.OrderGoodsTrxMapper;
import com.api.app.repository.pay.PayBaseMapper;
import com.api.app.service.payment.method.PaymentMethodFactory;
import com.api.app.service.payment.method.PaymentMethodStrategy;
import com.api.app.service.payment.strategy.PaymentGatewayFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 주문 서비스 구현
 *
 * @author Claude
 * @version 1.0
 * @since 2025-10-31
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderBaseTrxMapper orderBaseTrxMapper;
    private final OrderDetailTrxMapper orderDetailTrxMapper;
    private final OrderGoodsTrxMapper orderGoodsTrxMapper;
    private final OrderBaseMapper orderBaseMapper;
    private final OrderGoodsMapper orderGoodsMapper;
    private final PayBaseMapper payBaseMapper;
    private final MemberBaseMapper memberBaseMapper;
    private final GoodsItemMapper goodsItemMapper;
    private final GoodsPriceHistMapper goodsPriceHistMapper;
    private final BasketBaseTrxMapper basketBaseTrxMapper;
    private final PaymentMethodFactory paymentMethodFactory;
    private final PaymentGatewayFactory paymentGatewayFactory;

    @Override
    public String generateOrderNumber() {
        String orderNo = orderBaseTrxMapper.generateOrderNo();
        log.info("Order number generated: {}", orderNo);
        return orderNo;
    }

    @Override
    @Transactional
    public void createOrder(OrderRequest request) {
        log.info("Order creation started. memberNo={}, orderNo={}, goodsCount={}",
                request.getMemberNo(), request.getOrderNo(), request.getGoodsList().size());

        // 1. 프론트에서 전달받은 주문번호 사용
        String orderNo = request.getOrderNo();

        try {
            // 2. 검증
            validateOrder(request);

            // 3. 결제 처리 (우선순위 순서대로)
            List<PayBase> completedPayments = new ArrayList<>();
            processPayments(request, orderNo, completedPayments);

            // 4. 주문 엔티티 생성 및 저장
            createOrderEntities(request, orderNo);

            // 5. 후처리
            postProcess(request);

            log.info("Order creation completed successfully. orderNo={}", orderNo);

        } catch (Exception e) {
            log.error("Order creation failed. Rolling back payments. orderNo={}", orderNo, e);
            // 망취소 처리 필요 시 (카드 결제만)
            // TODO: 실제로는 completedPayments를 순회하며 망취소 호출
            throw e;
        }
    }

    /**
     * 주문 검증
     */
    private void validateOrder(OrderRequest request) {
        // 회원 검증
        MemberBase member = memberBaseMapper.selectMemberBaseByMemberNo(request.getMemberNo());
        if (member == null) {
            throw new IllegalArgumentException("회원 정보를 찾을 수 없습니다");
        }
        if (!MEM001.ACTIVE.getCode().equals(member.getMemberStatusCode())) {
            throw new IllegalArgumentException("정상 회원만 주문할 수 있습니다");
        }

        // 상품별 검증
        for (BasketResponse goods : request.getGoodsList()) {
            // 재고 검증
            GoodsItem item = goodsItemMapper.selectGoodsItemByKey(goods.getGoodsNo(), goods.getItemNo());
            if (item == null) {
                throw new IllegalArgumentException(
                        String.format("상품 정보를 찾을 수 없습니다: %s-%s", goods.getGoodsNo(), goods.getItemNo()));
            }
            if (item.getStock() < goods.getQuantity()) {
                throw new IllegalArgumentException(
                        String.format("재고가 부족합니다: %s (재고: %d, 요청: %d)",
                                goods.getGoodsName(), item.getStock(), goods.getQuantity()));
            }

            // 가격 검증 (가격 조작 방지)
            GoodsPriceHist priceHist = goodsPriceHistMapper.selectCurrentPrice(goods.getGoodsNo());
            if (priceHist == null) {
                throw new IllegalArgumentException("상품 가격 정보를 찾을 수 없습니다: " + goods.getGoodsNo());
            }
            Long expectedPrice = priceHist.getSalePrice() + item.getItemPrice();
            if (!expectedPrice.equals(goods.getSalePrice())) {
                throw new IllegalArgumentException(
                        String.format("가격이 변경되었습니다: %s (DB: %d, 요청: %d)",
                                goods.getGoodsName(), expectedPrice, goods.getSalePrice()));
            }

            // 장바구니 검증
            if (goods.getBasketNo() != null && !goods.getIsOrder()) {
                // 장바구니 상품이 이미 주문되지 않았는지 확인
                // TODO: Basket 조회 로직 필요시 추가
            }
        }
    }

    /**
     * 결제 처리 (우선순위 순서대로)
     */
    private void processPayments(OrderRequest request, String orderNo, List<PayBase> completedPayments) {
        // PAY002의 displaySequence 순서대로 정렬 (카드 우선, 포인트 후순위)
        List<PayRequest> sortedPayments = request.getPayList().stream()
                .sorted(Comparator.comparingInt(p ->
                        PAY002.findByCode(p.getPayWayCode()).getDisplaySequence()))
                .toList();

        for (PayRequest payRequest : sortedPayments) {
            try {
                PaymentMethodStrategy strategy = paymentMethodFactory.getStrategy(payRequest.getPayWayCode());
                PayBase payBase = strategy.processPayment(request.getMemberNo(), orderNo, payRequest);
                completedPayments.add(payBase);

                log.info("Payment processed successfully. payWayCode={}, amount={}",
                        payRequest.getPayWayCode(), payRequest.getAmount());
            } catch (Exception e) {
                log.error("Payment failed. payWayCode={}", payRequest.getPayWayCode(), e);

                // 이전에 완료된 결제들 망취소
                rollbackCompletedPayments(completedPayments);

                throw new RuntimeException("결제 처리에 실패했습니다: " + e.getMessage(), e);
            }
        }
    }

    /**
     * 완료된 결제 망취소
     */
    private void rollbackCompletedPayments(List<PayBase> completedPayments) {
        for (int i = completedPayments.size() - 1; i >= 0; i--) {
            PayBase payBase = completedPayments.get(i);

            // 카드 결제만 망취소 (포인트는 DB 롤백으로 처리)
            if (PAY002.CREDIT_CARD.getCode().equals(payBase.getPayWayCode())) {
                try {
                    log.warn("Initiating network cancellation for payment: {}", payBase.getPayNo());

                    // TODO: PG사 망취소 API 호출
                    // PaymentGatewayStrategy strategy = paymentGatewayFactory.getStrategy(payBase.getPgTypeCode());
                    // strategy.cancelPayment(confirmRequest);

                    log.warn("Network cancellation completed (TODO): {}", payBase.getPayNo());
                } catch (Exception e) {
                    log.error("Network cancellation failed for payment: {}", payBase.getPayNo(), e);
                    // 망취소 실패는 무시 (요구사항에 따라)
                }
            }
        }
    }

    /**
     * 주문 엔티티 생성 및 저장
     */
    private void createOrderEntities(OrderRequest request, String orderNo) {
        LocalDateTime now = LocalDateTime.now();

        // OrderBase 생성
        OrderBase orderBase = new OrderBase();
        orderBase.setOrderNo(orderNo);
        orderBase.setMemberNo(request.getMemberNo());
        orderBase.setRegistId(request.getMemberNo());
        orderBase.setRegistDateTime(now);
        orderBase.setModifyId(request.getMemberNo());
        orderBase.setModifyDateTime(now);

        int result = orderBaseTrxMapper.insertOrderBase(orderBase);
        if (result != 1) {
            throw new RuntimeException("주문 기본 정보 저장에 실패했습니다");
        }

        // OrderDetail 및 OrderGoods 생성
        long orderSequence = 1;
        for (BasketResponse goods : request.getGoodsList()) {
            // OrderDetail 생성
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderNo(orderNo);
            orderDetail.setOrderSequence(orderSequence);
            orderDetail.setOrderProcessSequence(1L);
            orderDetail.setGoodsNo(goods.getGoodsNo());
            orderDetail.setItemNo(goods.getItemNo());
            orderDetail.setQuantity(goods.getQuantity());
            orderDetail.setOrderStatusCode(ORD002.ORDER_RECEIVED.getCode());
            orderDetail.setDeliveryTypeCode("001"); // TODO: DLV001 enum 사용
            orderDetail.setOrderTypeCode(ORD001.ORDER.getCode());
            orderDetail.setOrderAcceptDtm(now);
            orderDetail.setRegistId(request.getMemberNo());
            orderDetail.setRegistDateTime(now);
            orderDetail.setModifyId(request.getMemberNo());
            orderDetail.setModifyDateTime(now);

            result = orderDetailTrxMapper.insertOrderDetail(orderDetail);
            if (result != 1) {
                throw new RuntimeException("주문 상세 정보 저장에 실패했습니다");
            }

            // OrderGoods 생성
            GoodsPriceHist priceHist = goodsPriceHistMapper.selectCurrentPrice(goods.getGoodsNo());

            OrderGoods orderGoods = new OrderGoods();
            orderGoods.setOrderNo(orderNo);
            orderGoods.setGoodsNo(goods.getGoodsNo());
            orderGoods.setItemNo(goods.getItemNo());
            orderGoods.setSalePrice(goods.getSalePrice());
            orderGoods.setSupplyPrice(priceHist.getSupplyPrice());
            orderGoods.setGoodsName(goods.getGoodsName());
            orderGoods.setItemName(goods.getItemName());
            orderGoods.setRegistId(request.getMemberNo());
            orderGoods.setRegistDateTime(now);
            orderGoods.setModifyId(request.getMemberNo());
            orderGoods.setModifyDateTime(now);

            result = orderGoodsTrxMapper.insertOrderGoods(orderGoods);
            if (result != 1) {
                throw new RuntimeException("주문 상품 정보 저장에 실패했습니다");
            }

            orderSequence++;
        }
    }

    /**
     * 후처리 (장바구니 주문 완료 처리 등)
     */
    private void postProcess(OrderRequest request) {
        // 장바구니 주문 완료 처리
        for (BasketResponse goods : request.getGoodsList()) {
            if (goods.getBasketNo() != null) {
                int result = basketBaseTrxMapper.updateBasketIsOrder(goods.getBasketNo(), request.getMemberNo());
                if (result != 1) {
                    log.warn("Failed to update basket is_order. basketNo={}", goods.getBasketNo());
                } else {
                    log.info("Basket order completed successfully. basketNo={}", goods.getBasketNo());
                }
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public OrderCompleteResponse getOrderComplete(String orderNo, String memberNo) {
        log.info("Retrieving order complete information. orderNo={}, memberNo={}", orderNo, memberNo);

        // 주문 기본 정보 조회
        OrderCompleteResponse response = orderBaseMapper.selectOrderCompleteByOrderNo(orderNo, memberNo);

        if (response == null) {
            log.error("Order not found or unauthorized access. orderNo={}, memberNo={}", orderNo, memberNo);
            throw new IllegalArgumentException("주문 정보를 찾을 수 없습니다");
        }

        // 주문 상품 목록 조회
        List<OrderCompleteResponse.OrderCompleteGoods> goodsList = orderGoodsMapper.selectOrderCompleteGoodsByOrderNo(orderNo);
        response.setGoodsList(goodsList);

        // 결제 정보 목록 조회
        List<OrderCompleteResponse.OrderCompletePayment> paymentList = payBaseMapper.selectOrderCompletePaymentByOrderNo(orderNo);
        response.setPaymentList(paymentList);

        log.info("Order complete information retrieved successfully. orderNo={}", orderNo);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderListResponse> getOrderList(String memberNo) {
        log.info("Retrieving order list. memberNo={}", memberNo);

        // TODO: XML 매퍼에 selectOrderListByMemberNo 쿼리 구현 필요
        List<OrderListResponse> orderList = orderBaseMapper.selectOrderListByMemberNo(memberNo);

        log.info("Order list retrieved successfully. memberNo={}, count={}", memberNo, orderList.size());

        return orderList;
    }
}
