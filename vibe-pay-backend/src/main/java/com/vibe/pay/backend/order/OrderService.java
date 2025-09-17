package com.vibe.pay.backend.order;

import com.vibe.pay.backend.payment.Payment;
import com.vibe.pay.backend.payment.PaymentService;
import com.vibe.pay.backend.payment.PaymentConfirmRequest;
import com.vibe.pay.backend.product.Product;
import com.vibe.pay.backend.product.ProductService;
import com.vibe.pay.backend.rewardpoints.RewardPointsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private ProductService productService;

    @Autowired
    private RewardPointsService rewardPointsService;

    @Autowired
    private PaymentService paymentService;


    public Optional<Order> getOrderById(Long id) {
        return Optional.ofNullable(orderMapper.findById(id));
    }

    public List<Order> getOrdersByMemberId(Long memberId) {
        return orderMapper.findByMemberId(memberId);
    }

    public List<Order> findAll() {
        return orderMapper.findAll();
    }

    @Transactional
    public Order cancelOrder(Long orderId) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new RuntimeException("Order not found with id " + orderId);
        }

        if (!"CANCELLED".equals(order.getStatus())) {
            // 1. Cancel associated payment
            if (order.getPaymentId() != null) {
                paymentService.cancelPayment(order.getPaymentId());
            }

            // 2. Refund reward points
            if (order.getUsedRewardPoints() > 0) {
                rewardPointsService.addPoints(order.getMemberId(), order.getUsedRewardPoints());
            }

            // 3. Update order status
            order.setStatus("CANCELLED");
            orderMapper.update(order);
        }
        return order;
    }

    // 1. 주문 번호 채번 (날짜 + O + 시퀀스)
    public String generateOrderNumber() {
        // 현재 날짜를 YYYYMMDD 형식으로 포맷
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        // DB 시퀀스에서 다음 번호 조회
        Long sequence = orderMapper.getNextOrderSequence();
        
        // 8자리로 패딩 (00000001 ~ 99999999)
        String sequenceStr = String.format("%08d", sequence);
        
        return dateStr + "O" + sequenceStr;
    }

    // 주문 생성 + 결제 승인
    @Transactional
    public Order createOrder(OrderRequest orderRequest) {
        // 4-1. 먼저 결제 승인 처리
        PaymentConfirmRequest paymentConfirmRequest = new PaymentConfirmRequest();
        paymentConfirmRequest.setAuthToken(orderRequest.getAuthToken());
        paymentConfirmRequest.setAuthUrl(orderRequest.getAuthUrl());
        paymentConfirmRequest.setOid(orderRequest.getOid());
        paymentConfirmRequest.setPrice(orderRequest.getPrice());
        paymentConfirmRequest.setMid(orderRequest.getMid());
        paymentConfirmRequest.setOrderNumber(orderRequest.getOrderNumber());
        paymentConfirmRequest.setNetCancelUrl(orderRequest.getNetCancelUrl());

        // 결제 승인 처리
        Payment payment;
        try {
            payment = paymentService.confirmPayment(paymentConfirmRequest);
        } catch (Exception e) {
            throw new RuntimeException("Payment approval failed: " + e.getMessage(), e);
        }

        // 4-2. 결제 승인 성공 시 주문 생성
        Order order = new Order();
        order.setMemberId(orderRequest.getMemberId());
        order.setUsedRewardPoints(orderRequest.getUsedPoints() != null ? orderRequest.getUsedPoints().doubleValue() : 0.0);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PAID"); // 결제 완료 상태
        order.setPaymentId(payment.getId());

        // 주문 상품 정보 처리 및 총액 계산
        double calculatedTotalAmount = 0.0;
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemRequest itemRequest : orderRequest.getItems()) {
            Product product = productService.getProductById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found with id " + itemRequest.getProductId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(itemRequest.getProductId());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPriceAtOrder(product.getPrice());

            orderItems.add(orderItem);
            calculatedTotalAmount += product.getPrice() * orderItem.getQuantity();
        }
        order.setTotalAmount(calculatedTotalAmount);

        // 적립금 사용 처리
        if (order.getUsedRewardPoints() > 0) {
            rewardPointsService.usePoints(order.getMemberId(), order.getUsedRewardPoints());
            order.setFinalPaymentAmount(order.getTotalAmount() - order.getUsedRewardPoints());
        } else {
            order.setFinalPaymentAmount(order.getTotalAmount());
        }

        // 최종 결제 금액이 음수가 되지 않도록 보정
        if (order.getFinalPaymentAmount() < 0) {
            order.setFinalPaymentAmount(0.0);
        }

        // 주문 저장
        orderMapper.insert(order);

        // 주문 상품 저장
        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }

        return order;
    }
}
