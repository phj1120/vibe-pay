package com.vibe.pay.backend.order;

import com.vibe.pay.backend.payment.Payment;
import com.vibe.pay.backend.payment.PaymentService;
import com.vibe.pay.backend.product.Product;
import com.vibe.pay.backend.product.ProductService;
import com.vibe.pay.backend.rewardpoints.RewardPointsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    @Transactional
    public Order createOrder(OrderRequest orderRequest) {
        // 1. Create Order entity from OrderRequest
        Order order = new Order();
        order.setMemberId(orderRequest.getMemberId());
        order.setUsedRewardPoints(orderRequest.getUsedPoints());
        order.setOrderDate(LocalDateTime.now());
        
        // 결제가 이미 완료된 경우 PAID 상태로 설정
        if (orderRequest.getPaymentId() != null) {
            order.setStatus("PAID");
            order.setPaymentId(orderRequest.getPaymentId());
        } else {
            order.setStatus("PENDING");
        }

        // 2. Calculate total amount and create OrderItem entities
        double calculatedTotalAmount = 0.0;
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemRequest itemRequest : orderRequest.getItems()) {
            Product product = productService.getProductById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found with id " + itemRequest.getProductId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(itemRequest.getProductId());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPriceAtOrder(product.getPrice()); // Store price at the time of order

            orderItems.add(orderItem);
            calculatedTotalAmount += product.getPrice() * orderItem.getQuantity();
        }
        order.setTotalAmount(calculatedTotalAmount);

        // 3. Handle reward points usage and final payment amount
        if (order.getUsedRewardPoints() > 0) {
            rewardPointsService.usePoints(order.getMemberId(), order.getUsedRewardPoints());
            order.setFinalPaymentAmount(order.getTotalAmount() - order.getUsedRewardPoints());
        } else {
            order.setFinalPaymentAmount(order.getTotalAmount());
        }

        // Ensure final payment amount is not negative
        if (order.getFinalPaymentAmount() < 0) {
            order.setFinalPaymentAmount(0.0);
        }

        // 4. Save order (first insert to get ID)
        orderMapper.insert(order);

        // 5. Save order items
        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }

        // 6. Create payment entry only if paymentId is not provided
        if (orderRequest.getPaymentId() == null) {
            Payment payment = new Payment(
                    order.getMemberId(),
                    order.getFinalPaymentAmount(),
                    "CREDIT_CARD", // Default payment method for now
                    "NICEPAY",     // Default PG for now
                    "PENDING",
                    null // Transaction ID will be set after actual PG processing
            );
            paymentService.createPayment(payment);

            // 7. Link payment to order and update order
            order.setPaymentId(payment.getId());
            orderMapper.update(order); // Update order with paymentId
        }

        return order;
    }

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
}
