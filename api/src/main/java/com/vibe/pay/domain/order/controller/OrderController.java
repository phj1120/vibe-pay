package com.vibe.pay.domain.order.controller;

import com.vibe.pay.domain.order.dto.OrderDetailDto;
import com.vibe.pay.domain.order.dto.OrderRequest;
import com.vibe.pay.domain.order.dto.OrderResponse;
import com.vibe.pay.domain.order.entity.Order;
import com.vibe.pay.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 주문 관리 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/generateOrderNumber")
    public ResponseEntity<String> generateOrderNumber() {
        log.info("Generating order number");
        return ResponseEntity.ok(orderService.generateOrderNumber());
    }

    @PostMapping
    public ResponseEntity<List<OrderResponse>> createOrder(@RequestBody OrderRequest request) {
        log.info("Creating order for member: {}", request.getMemberId());
        try {
            List<Order> orders = orderService.createOrder(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(orders.stream().map(this::toResponse).collect(Collectors.toList()));
        } catch (RuntimeException e) {
            log.error("Order creation failed: {}", e.getMessage());
            return e.getMessage().contains("결제 승인 실패")
                    ? ResponseEntity.badRequest().build()
                    : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable String orderId) {
        log.info("Cancelling order: {}", orderId);
        try {
            Order order = orderService.cancelOrder(orderId);
            return ResponseEntity.ok(toResponse(order));
        } catch (RuntimeException e) {
            log.error("Order cancellation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        log.info("Getting all orders");
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<List<OrderResponse>> getOrderById(@PathVariable String orderId) {
        log.info("Getting order by ID: {}", orderId);
        List<Order> orders = orderService.getOrderById(orderId);
        if (orders.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(orders.stream().map(this::toResponse).collect(Collectors.toList()));
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByMemberId(@PathVariable Long memberId) {
        log.info("Getting orders by member ID: {}", memberId);
        List<Order> orders = orderService.getOrdersByMemberId(memberId);
        return ResponseEntity.ok(orders.stream().map(this::toResponse).collect(Collectors.toList()));
    }

    @GetMapping("/member/{memberId}/details")
    public ResponseEntity<List<OrderDetailDto>> getOrderDetailsWithPaymentsByMemberId(@PathVariable Long memberId) {
        log.info("Getting order details with payments by member ID: {}", memberId);
        return ResponseEntity.ok(orderService.getOrderDetailsWithPaymentsByMemberId(memberId));
    }

    private OrderResponse toResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getOrderId());
        response.setOrdSeq(order.getOrdSeq());
        response.setOrdProcSeq(order.getOrdProcSeq());
        response.setClaimId(order.getClaimId());
        response.setMemberId(order.getMemberId());
        response.setOrderDate(order.getOrderDate());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        return response;
    }
}