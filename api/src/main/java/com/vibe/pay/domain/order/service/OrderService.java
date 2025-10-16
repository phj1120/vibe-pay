package com.vibe.pay.domain.order.service;

import com.vibe.pay.domain.order.dto.OrderDetailDto;
import com.vibe.pay.domain.order.dto.OrderItemDto;
import com.vibe.pay.domain.order.dto.OrderItemRequest;
import com.vibe.pay.domain.order.dto.OrderRequest;
import com.vibe.pay.domain.order.entity.Order;
import com.vibe.pay.domain.order.entity.OrderItem;
import com.vibe.pay.domain.order.repository.OrderItemMapper;
import com.vibe.pay.domain.order.repository.OrderMapper;
import com.vibe.pay.domain.payment.dto.PaymentMethodRequest;
import com.vibe.pay.domain.payment.entity.Payment;
import com.vibe.pay.domain.payment.service.PaymentService;
import com.vibe.pay.domain.product.entity.Product;
import com.vibe.pay.domain.product.service.ProductService;
import com.vibe.pay.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 주문 서비스
 * 주문 생성, 취소, 조회 등의 비즈니스 로직을 처리하는 계층
 *
 * Technical Specification 참조:
 * - docs/v5/TechnicalSpecification/order-processing-spec.md
 *
 * @see Order
 * @see OrderItem
 * @see OrderMapper
 * @see OrderItemMapper
 * @see PaymentService
 * @see ProductService
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductService productService;
    private final PaymentService paymentService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final int SEQUENCE_PADDING_LENGTH = 8;

    /**
     * 주문 번호 생성
     * 형식: YYYYMMDDOXXXXXXXX (날짜 + O + 8자리 시퀀스)
     *
     * @return 생성된 주문 번호
     * @throws RuntimeException 시퀀스 조회 실패 시
     */
    public String generateOrderNumber() {
        String dateStr = LocalDateTime.now().format(DATE_FORMATTER);
        Long sequence = orderMapper.getNextOrderSequence();
        String sequenceStr = String.format("%0" + SEQUENCE_PADDING_LENGTH + "d", sequence);
        String orderNumber = dateStr + "O" + sequenceStr;

        log.debug("Generated order number: {}", orderNumber);
        return orderNumber;
    }

    /**
     * 클레임 번호 생성
     * 형식: YYYYMMDDCXXXXXXXX (날짜 + C + 8자리 시퀀스)
     *
     * @return 생성된 클레임 번호
     * @throws RuntimeException 시퀀스 조회 실패 시
     */
    public String generateClaimNumber() {
        String dateStr = LocalDateTime.now().format(DATE_FORMATTER);
        Long sequence = orderMapper.getNextClaimSequence();
        String sequenceStr = String.format("%0" + SEQUENCE_PADDING_LENGTH + "d", sequence);
        String claimNumber = dateStr + "C" + sequenceStr;

        log.debug("Generated claim number: {}", claimNumber);
        return claimNumber;
    }

    /**
     * 주문 생성
     *
     * 프로세스:
     * 1. 결제 승인 처리 (PaymentService.confirmPayment)
     * 2. 상품 정보 조회 및 주문 생성
     * 3. DB 저장
     * 4. 실패 시 망취소 처리
     *
     * TODO: PG 연동 망취소 로직 구현 필요 (payment-and-pg-integration-spec.md 참조)
     *
     * @param request 주문 생성 요청 DTO
     * @return 생성된 주문 엔티티 목록
     * @throws RuntimeException 결제 승인 실패, 상품 조회 실패, 주문 생성 실패 시
     */
    @Transactional
    public List<Order> createOrder(OrderRequest request) {
        log.info("Creating order: orderNumber={}, memberId={}", request.getOrderNumber(), request.getMemberId());

        List<Order> createdOrders = new ArrayList<>();

        try {
            // 1. 결제 승인 처리
            for (PaymentMethodRequest paymentMethod : request.getPaymentMethods()) {
                log.debug("Processing payment: method={}, amount={}",
                        paymentMethod.getPaymentMethod(), paymentMethod.getAmount());
                // TODO: PaymentService.confirmPayment() 호출
                // paymentService.confirmPayment(paymentMethod);
            }

            // 2. 주문 생성
            int ordSeq = 1;
            for (OrderItemRequest itemRequest : request.getItems()) {
                // 상품 정보 조회
                Product product = productService.getProductById(itemRequest.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found: " + itemRequest.getProductId()));

                // Order 엔티티 생성
                Order order = new Order();
                order.setOrderId(request.getOrderNumber());
                order.setOrdSeq(ordSeq);
                order.setOrdProcSeq(1); // 최초 주문
                order.setMemberId(request.getMemberId());
                order.setOrderDate(LocalDateTime.now());
                order.setTotalAmount(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
                order.setStatus(OrderStatus.ORDERED);

                orderMapper.insert(order);
                log.debug("Order created: orderId={}, ordSeq={}", order.getOrderId(), order.getOrdSeq());

                // OrderItem 엔티티 생성
                OrderItem orderItem = new OrderItem();
                orderItem.setOrderId(order.getOrderId());
                orderItem.setOrdSeq(order.getOrdSeq());
                orderItem.setOrdProcSeq(1);
                orderItem.setProductId(product.getProductId());
                orderItem.setQuantity(itemRequest.getQuantity());
                orderItem.setPriceAtOrder(product.getPrice());

                orderItemMapper.insert(orderItem);
                log.debug("OrderItem created: orderItemId={}", orderItem.getOrderItemId());

                createdOrders.add(order);
                ordSeq++;
            }

            log.info("Order created successfully: orderNumber={}, totalItems={}",
                    request.getOrderNumber(), createdOrders.size());

            return createdOrders;

        } catch (Exception e) {
            log.error("Order creation failed, attempting net cancel: orderNumber={}", request.getOrderNumber(), e);

            // TODO: 망취소 처리 구현 필요
            // for (PaymentMethodRequest paymentMethod : request.getPaymentMethods()) {
            //     try {
            //         paymentService.netCancel(paymentMethod);
            //     } catch (Exception netCancelException) {
            //         log.error("Net cancel failed", netCancelException);
            //     }
            // }

            throw new RuntimeException("Order creation failed: " + e.getMessage(), e);
        }
    }

    /**
     * 주문 취소
     *
     * 프로세스:
     * 1. 원본 주문 조회 및 검증
     * 2. 클레임 번호 생성
     * 3. 취소 주문 생성 (음수 금액, ordProcSeq+1)
     * 4. 취소 주문 상품 생성 (음수 수량)
     * 5. 결제 환불 처리
     *
     * TODO: 결제 환불 연동 구현 필요 (payment-and-pg-integration-spec.md 참조)
     *
     * @param orderId 취소할 주문 ID
     * @return 취소된 주문 엔티티
     * @throws RuntimeException 주문을 찾을 수 없거나 이미 취소되었거나 취소 실패 시
     */
    @Transactional
    public Order cancelOrder(String orderId) {
        log.info("Cancelling order: orderId={}", orderId);

        // 1. 원본 주문 조회
        List<Order> originalOrders = orderMapper.findByOrderIdAndOrdProcSeq(orderId, 1);
        if (originalOrders.isEmpty()) {
            throw new RuntimeException("Order not found: " + orderId);
        }

        // 2. 이미 취소된 주문인지 확인
        List<Order> allOrders = orderMapper.findByOrderId(orderId);
        boolean alreadyCancelled = allOrders.stream()
                .anyMatch(order -> order.getOrdProcSeq() > 1);
        if (alreadyCancelled) {
            throw new RuntimeException("Order already cancelled: " + orderId);
        }

        // 3. 클레임 번호 생성
        String claimId = generateClaimNumber();
        log.debug("Generated claimId: {}", claimId);

        // 4. 취소 주문 생성
        Order firstCancelOrder = null;
        for (Order originalOrder : originalOrders) {
            Order cancelOrder = new Order();
            cancelOrder.setOrderId(originalOrder.getOrderId());
            cancelOrder.setOrdSeq(originalOrder.getOrdSeq());
            cancelOrder.setOrdProcSeq(originalOrder.getOrdProcSeq() + 1);
            cancelOrder.setClaimId(claimId);
            cancelOrder.setMemberId(originalOrder.getMemberId());
            cancelOrder.setOrderDate(LocalDateTime.now());
            cancelOrder.setTotalAmount(originalOrder.getTotalAmount().negate()); // 음수 금액
            cancelOrder.setStatus(OrderStatus.CANCELLED);

            orderMapper.insert(cancelOrder);
            log.debug("Cancel order created: orderId={}, ordSeq={}, ordProcSeq={}",
                    cancelOrder.getOrderId(), cancelOrder.getOrdSeq(), cancelOrder.getOrdProcSeq());

            if (firstCancelOrder == null) {
                firstCancelOrder = cancelOrder;
            }
        }

        // 5. 취소 주문 상품 생성
        List<OrderItem> originalItems = orderItemMapper.findByOrderIdAndOrdProcSeq(orderId, 1);
        for (OrderItem originalItem : originalItems) {
            OrderItem cancelItem = new OrderItem();
            cancelItem.setOrderId(originalItem.getOrderId());
            cancelItem.setOrdSeq(originalItem.getOrdSeq());
            cancelItem.setOrdProcSeq(2); // 취소
            cancelItem.setProductId(originalItem.getProductId());
            cancelItem.setQuantity(-originalItem.getQuantity()); // 음수 수량
            cancelItem.setPriceAtOrder(originalItem.getPriceAtOrder());

            orderItemMapper.insert(cancelItem);
            log.debug("Cancel order item created: orderItemId={}", cancelItem.getOrderItemId());
        }

        // 6. 결제 환불 처리
        List<Payment> payments = paymentService.findByOrderId(orderId);
        for (Payment payment : payments) {
            log.debug("Processing refund: paymentId={}", payment.getPaymentId());
            payment.setClaimId(claimId);
            // TODO: PaymentService.processRefund() 호출
            // paymentService.processRefund(payment);
        }

        log.info("Order cancelled successfully: orderId={}, claimId={}", orderId, claimId);
        return firstCancelOrder;
    }

    /**
     * 회원별 주문 목록 조회
     *
     * @param memberId 회원 ID
     * @return 주문 엔티티 목록
     */
    public List<Order> getOrdersByMemberId(Long memberId) {
        log.debug("Fetching orders by memberId: {}", memberId);
        return orderMapper.findByMemberId(memberId);
    }

    /**
     * 주문 ID로 주문 조회
     *
     * @param orderId 주문 ID
     * @return 주문 엔티티 목록
     */
    public List<Order> getOrderById(String orderId) {
        log.debug("Fetching order by orderId: {}", orderId);
        return orderMapper.findByOrderId(orderId);
    }

    /**
     * 회원별 주문 상세 조회 (결제 정보 포함)
     *
     * 주문, 주문 상품, 결제 정보, 주문 처리 이력을 포함한 상세 정보 조회
     *
     * TODO: N+1 쿼리 최적화 필요 (MyBatis association/collection 활용)
     *
     * @param memberId 회원 ID
     * @return 주문 상세 DTO 목록
     */
    public List<OrderDetailDto> getOrderDetailsWithPaymentsByMemberId(Long memberId) {
        log.debug("Fetching order details with payments by memberId: {}", memberId);

        // 1. 원본 주문 조회 (ordProcSeq = 1)
        List<Order> originalOrders = orderMapper.getOrderDetailsWithPaymentsByMemberId(memberId);
        List<OrderDetailDto> orderDetails = new ArrayList<>();

        for (Order originalOrder : originalOrders) {
            OrderDetailDto detailDto = new OrderDetailDto();
            detailDto.setOrderId(originalOrder.getOrderId());
            detailDto.setOrdSeq(originalOrder.getOrdSeq());
            detailDto.setOrdProcSeq(originalOrder.getOrdProcSeq());
            detailDto.setClaimId(originalOrder.getClaimId());
            detailDto.setMemberId(originalOrder.getMemberId());
            detailDto.setOrderDate(originalOrder.getOrderDate());
            detailDto.setTotalAmount(originalOrder.getTotalAmount());
            detailDto.setStatus(originalOrder.getStatus());

            // 2. 주문 처리 이력 조회
            List<Order> orderProcesses = orderMapper.findByOrderId(originalOrder.getOrderId());
            detailDto.setOrderProcesses(orderProcesses);

            // 3. 주문 상품 정보 조회
            List<OrderItem> orderItems = orderItemMapper.findByOrderIdAndOrdProcSeq(originalOrder.getOrderId(), 1);
            List<OrderItemDto> orderItemDtos = new ArrayList<>();
            for (OrderItem item : orderItems) {
                OrderItemDto itemDto = new OrderItemDto();
                itemDto.setOrderItemId(item.getOrderItemId());
                itemDto.setOrderId(item.getOrderId());
                itemDto.setOrdSeq(item.getOrdSeq());
                itemDto.setOrdProcSeq(item.getOrdProcSeq());
                itemDto.setProductId(item.getProductId());
                itemDto.setPriceAtOrder(item.getPriceAtOrder());
                itemDto.setQuantity(item.getQuantity());
                itemDto.setTotalPrice(item.getPriceAtOrder().multiply(BigDecimal.valueOf(item.getQuantity())));

                // 상품명 조회
                Product product = productService.getProductById(item.getProductId()).orElse(null);
                if (product != null) {
                    itemDto.setProductName(product.getName());
                }

                orderItemDtos.add(itemDto);
            }
            detailDto.setOrderItems(orderItemDtos);

            // 4. 결제 정보 조회
            List<Payment> payments = paymentService.findByOrderId(originalOrder.getOrderId());
            detailDto.setPayments(payments);

            orderDetails.add(detailDto);
        }

        log.debug("Order details fetched: memberId={}, totalOrders={}", memberId, orderDetails.size());
        return orderDetails;
    }
}
