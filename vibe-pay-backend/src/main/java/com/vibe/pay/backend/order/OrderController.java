package com.vibe.pay.backend.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    @Autowired
    private OrderService orderService;

    // 주문번호 채번 API
    @GetMapping("/generateOrderNumber")
    public ResponseEntity<String> generateOrderNumber() {
        try {
            String orderNumber = orderService.generateOrderNumber();
            return ResponseEntity.ok(orderNumber);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 주문 생성 API (결제 승인 로직 포함)
    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest orderRequest) {
        try {
            Order createdOrder = orderService.createOrder(orderRequest);
            return ResponseEntity.ok(createdOrder);
        } catch (RuntimeException e) {
            log.error("createOrder failed for orderNumber={}", orderRequest.getOrderNumber(), e);
            return ResponseEntity.badRequest().body(null); // Or a more specific error response
        }
    }

    @GetMapping
    public List<Order> getAllOrders() {
        // This might not be ideal for production, consider pagination
        return orderService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable String id) {
        return orderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/member/{memberId}")
    public List<Order> getOrdersByMemberId(@PathVariable Long memberId) {
        return orderService.getOrdersByMemberId(memberId);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable String id) {
        try {
            Order cancelledOrder = orderService.cancelOrder(id);
            return ResponseEntity.ok(cancelledOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}