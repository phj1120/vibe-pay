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
            
            // 에러 메시지에 따른 상세한 응답 처리
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("Payment approval failed")) {
                // 결제 승인 실패는 클라이언트 에러로 처리
                return ResponseEntity.badRequest().body(null);
            } else if (errorMessage != null && errorMessage.contains("Order creation failed after payment approval")) {
                // 결제 승인 후 주문 생성 실패는 서버 에러로 처리 (망취소 시도됨)
                return ResponseEntity.internalServerError().body(null);
            } else {
                // 기타 에러는 일반적인 클라이언트 에러로 처리
                return ResponseEntity.badRequest().body(null);
            }
        }
    }

    @GetMapping
    public List<Order> getAllOrders() {
        // This might not be ideal for production, consider pagination
        return orderService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<Order>> getOrderById(@PathVariable String id) {
        List<Order> orders = orderService.getOrderById(id);
        if (orders.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/member/{memberId}")
    public List<Order> getOrdersByMemberId(@PathVariable Long memberId) {
        return orderService.getOrdersByMemberId(memberId);
    }

    @GetMapping("/member/{memberId}/details")
    public List<OrderDetailDto> getOrderDetailsWithPaymentsByMemberId(@PathVariable Long memberId) {
        return orderService.getOrderDetailsWithPaymentsByMemberId(memberId);
    }

    @GetMapping("/details/{orderId}")
    public ResponseEntity<List<OrderDetailDto>> getOrderDetailsByOrderId(@PathVariable String orderId) {
        try {
            // 주문번호로 회원 ID 조회 후 해당 주문의 상세 정보 반환
            List<Order> orders = orderService.getOrderById(orderId);
            if (orders.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Long memberId = orders.get(0).getMemberId();
            List<OrderDetailDto> allOrderDetails = orderService.getOrderDetailsWithPaymentsByMemberId(memberId);
            
            // 해당 주문번호와 일치하는 주문 상세 정보만 필터링
            List<OrderDetailDto> filteredDetails = allOrderDetails.stream()
                    .filter(detail -> orderId.equals(detail.getOrderId()))
                    .toList();
            
            return ResponseEntity.ok(filteredDetails);
        } catch (Exception e) {
            log.error("Failed to get order details for orderId={}", orderId, e);
            return ResponseEntity.internalServerError().build();
        }
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