package com.vibe.pay.backend.order;

import com.vibe.pay.backend.payment.Payment;
import com.vibe.pay.backend.payment.PaymentService;
import com.vibe.pay.backend.payment.PaymentMapper;
import com.vibe.pay.backend.payment.PaymentConfirmRequest;
import com.vibe.pay.backend.payment.factory.PaymentGatewayFactory;
import com.vibe.pay.backend.payment.factory.PaymentProcessorFactory;
import com.vibe.pay.backend.payment.gateway.PaymentGatewayAdapter;
import com.vibe.pay.backend.payment.gateway.PaymentNetCancelRequest;
import com.vibe.pay.backend.payment.processor.PaymentProcessor;
import com.vibe.pay.backend.product.Product;
import com.vibe.pay.backend.product.ProductService;
import com.vibe.pay.backend.rewardpoints.RewardPointsService;
import com.vibe.pay.backend.exception.OrderException;
import com.vibe.pay.backend.common.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductService productService;
    private final PaymentMapper paymentMapper;
    private final PaymentService paymentService;
    private final PaymentProcessorFactory paymentProcessorFactory;
    private final PaymentGatewayFactory paymentGatewayFactory;

    public List<Order> getOrderById(String orderId) {
        return orderMapper.findByOrderId(orderId);
    }

    public List<Order> getOrdersByMemberId(Long memberId) {
        return orderMapper.findByMemberId(memberId);
    }

    /**
     * 회원별 주문 상세 정보 페이징 조회 (상품 정보 + 결제 정보 포함)
     */
    public List<OrderDetailDto> getOrderDetailsWithPaymentsByMemberIdWithPaging(Long memberId, int page, int size) {
        int offset = page * size;
        // 원본 주문만 조회 (ord_proc_seq = 1) with paging
        List<Order> originalOrders = orderMapper.findByMemberIdAndOrdProcSeqWithPaging(memberId, 1, offset, size);
        List<OrderDetailDto> orderDetails = new ArrayList<>();

        for (Order originalOrder : originalOrders) {
            OrderDetailDto orderDetail = new OrderDetailDto(originalOrder);

            // 동일 주문번호의 모든 처리건 조회 (주문 + 취소)
            List<Order> allOrderProcesses = orderMapper.findByOrderId(originalOrder.getOrderId());
            orderDetail.setOrderProcesses(allOrderProcesses);

            // 원본 주문의 상품 정보 조회 (ord_proc_seq = 1)
            List<OrderItem> orderItems = orderItemMapper.findByOrderIdAndOrdProcSeq(originalOrder.getOrderId(), 1);

            List<OrderItemDto> orderItemDtos = new ArrayList<>();
            for (OrderItem orderItem : orderItems) {
                Optional<Product> product = productService.getProductById(orderItem.getProductId());
                String productName = product.map(Product::getName).orElse("상품명 없음");
                orderItemDtos.add(new OrderItemDto(orderItem, productName));
            }
            orderDetail.setOrderItems(orderItemDtos);

            // 해당 주문의 모든 결제 정보 조회 (카드 + 포인트)
            List<Payment> payments = paymentMapper.findByOrderId(originalOrder.getOrderId());
            orderDetail.setPayments(payments);

            orderDetails.add(orderDetail);
        }

        return orderDetails;
    }

    /**
     * 회원별 주문 상세 정보 조회 (상품 정보 + 결제 정보 포함)
     */
    public List<OrderDetailDto> getOrderDetailsWithPaymentsByMemberId(Long memberId) {
        // 원본 주문만 조회 (ord_proc_seq = 1)
        List<Order> originalOrders = orderMapper.findByMemberIdAndOrdProcSeq(memberId, 1);
        List<OrderDetailDto> orderDetails = new ArrayList<>();

        for (Order originalOrder : originalOrders) {
            OrderDetailDto orderDetail = new OrderDetailDto(originalOrder);

            // 동일 주문번호의 모든 처리건 조회 (주문 + 취소)
            List<Order> allOrderProcesses = orderMapper.findByOrderId(originalOrder.getOrderId());
            orderDetail.setOrderProcesses(allOrderProcesses);

            // 원본 주문의 상품 정보 조회 (ord_proc_seq = 1)
            List<OrderItem> orderItems = orderItemMapper.findByOrderIdAndOrdProcSeq(originalOrder.getOrderId(), 1);

            List<OrderItemDto> orderItemDtos = new ArrayList<>();
            for (OrderItem orderItem : orderItems) {
                Optional<Product> product = productService.getProductById(orderItem.getProductId());
                String productName = product.map(Product::getName).orElse("상품명 없음");
                orderItemDtos.add(new OrderItemDto(orderItem, productName));
            }
            orderDetail.setOrderItems(orderItemDtos);

            // 해당 주문의 모든 결제 정보 조회 (카드 + 포인트)
            List<Payment> payments = paymentMapper.findByOrderId(originalOrder.getOrderId());
            orderDetail.setPayments(payments);

            orderDetails.add(orderDetail);
        }

        return orderDetails;
    }

    public List<Order> findAll() {
        return orderMapper.findAll();
    }

    @Transactional
    public Order cancelOrder(String orderId) {
        // 1. 원본 주문들 조회 (ord_proc_seq = 1인 모든 상품)
        List<Order> originalOrders = orderMapper.findByOrderIdAndOrdProcSeqList(orderId, 1);
        
        if (originalOrders.isEmpty()) {
            throw OrderException.orderNotFound(orderId);
        }

        // 2. 이미 취소된 주문인지 확인 (ord_proc_seq가 1보다 큰 경우가 있으면 이미 취소됨)
        List<Order> allOrdersForId = orderMapper.findByOrderId(orderId);
        boolean alreadyCancelled = allOrdersForId.stream()
                .anyMatch(order -> order.getOrdProcSeq() > 1);
        
        if (alreadyCancelled) {
            throw OrderException.alreadyCancelled(orderId);
        }

        // 3. 클레임 번호 생성
        String claimId = generateClaimNumber();

        // 4. 모든 원본 주문에 대해 취소건 주문 생성
        Order firstCancelOrder = null;
        for (Order originalOrder : originalOrders) {
            Order cancelOrder = new Order();
            cancelOrder.setOrderId(originalOrder.getOrderId());
            cancelOrder.setOrdSeq(originalOrder.getOrdSeq());
            cancelOrder.setOrdProcSeq(originalOrder.getOrdProcSeq() + 1); // +1 증가
            cancelOrder.setClaimId(claimId);
            cancelOrder.setMemberId(originalOrder.getMemberId());
            cancelOrder.setOrderDate(LocalDateTime.now()); // 취소 일시
            cancelOrder.setTotalAmount(-originalOrder.getTotalAmount()); // 마이너스 금액
            cancelOrder.setStatus("CANCELLED");

            // 5. 취소건 주문 저장
            orderMapper.insert(cancelOrder);
            
            if (firstCancelOrder == null) {
                firstCancelOrder = cancelOrder;
            }
        }

        // 6. 취소건 주문 상품들 생성
        List<OrderItem> originalOrderItems = orderItemMapper.findByOrderIdAndOrdProcSeq(orderId, 1);
        for (OrderItem originalItem : originalOrderItems) {
            OrderItem cancelItem = new OrderItem();
            cancelItem.setOrderId(originalItem.getOrderId());
            cancelItem.setOrdSeq(originalItem.getOrdSeq());
            cancelItem.setOrdProcSeq(2); // 취소건의 ord_proc_seq는 2
            cancelItem.setProductId(originalItem.getProductId());
            cancelItem.setQuantity(-originalItem.getQuantity()); // 마이너스 수량
            cancelItem.setPriceAtOrder(originalItem.getPriceAtOrder());
            
            orderItemMapper.insert(cancelItem);
        }

        // 7. 결제 취소 처리
        List<Payment> payments = paymentService.findByOrderId(orderId);
        for (Payment payment : payments) {
            payment.setClaimId(claimId);
            PaymentProcessor processor = paymentProcessorFactory.getProcessor(payment.getPaymentMethod());

            processor.processRefund(payment);
        }

        // TODO List 로 반환.
        return firstCancelOrder; // 첫 번째 취소건 주문 반환
    }

    // 1. 주문 번호 채번 (날짜 + O + 시퀀스)
    public String generateOrderNumber() {
        // 현재 날짜를 YYYYMMDD 형식으로 포맷
        String dateStr = LocalDateTime.now().format(Constants.DATE_FORMATTER_YYYYMMDD);
        
        // DB 시퀀스에서 다음 번호 조회
        Long sequence = orderMapper.getNextOrderSequence();
        
        // 8자리로 패딩 (00000001 ~ 99999999)
        String sequenceStr = String.format("%08d", sequence);
        
        return dateStr + "O" + sequenceStr;
    }
    
    // 2. 클레임 번호 채번 (날짜 + C + 시퀀스)
    public String generateClaimNumber() {
        // 현재 날짜를 YYYYMMDD 형식으로 포맷
        String dateStr = LocalDateTime.now().format(Constants.DATE_FORMATTER_YYYYMMDD);
        
        // DB 시퀀스에서 다음 번호 조회
        Long sequence = orderMapper.getNextClaimSequence();
        
        // 8자리로 패딩 (00000001 ~ 99999999)
        String sequenceStr = String.format("%08d", sequence);
        
        return dateStr + "C" + sequenceStr;
    }

    // 결제 처리 없이 주문만 생성 (Command 패턴용)
    @Transactional
    public List<Order> createOrderWithoutPayment(OrderRequest orderRequest) {
        try {
            log.info("Creating order without payment processing: {}", orderRequest.getOrderNumber());

            List<Order> orders = new ArrayList<>();
            List<OrderItem> orderItems = new ArrayList<>();
            int ordSeq = 1;

            for (OrderItemRequest itemRequest : orderRequest.getItems()) {
                Product product = productService.getProductById(itemRequest.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found with id " + itemRequest.getProductId()));

                // 상품별로 별도의 Order 생성
                Order order = new Order();
                order.setOrderId(orderRequest.getOrderNumber());
                order.setOrdSeq(ordSeq);
                order.setOrdProcSeq(1);
                order.setMemberId(orderRequest.getMemberId());
                order.setOrderDate(LocalDateTime.now());
                order.setTotalAmount(product.getPrice() * itemRequest.getQuantity());
                order.setStatus("ORDERED");

                orders.add(order);

                // OrderItem 생성
                OrderItem orderItem = new OrderItem();
                orderItem.setOrderId(orderRequest.getOrderNumber());
                orderItem.setOrdSeq(ordSeq);
                orderItem.setOrdProcSeq(1);
                orderItem.setProductId(itemRequest.getProductId());
                orderItem.setQuantity(itemRequest.getQuantity());
                orderItem.setPriceAtOrder(product.getPrice());

                orderItems.add(orderItem);
                ordSeq++;
            }

            // 주문 저장
            for (Order order : orders) {
                orderMapper.insert(order);
            }

            // 주문 상품 저장
            for (OrderItem item : orderItems) {
                orderItemMapper.insert(item);
            }

            return orders;

        } catch (Exception e) {
            log.error("Order creation failed: {}", e.getMessage(), e);
            throw OrderException.creationFailed(e.getMessage());
        }
    }

    // 주문 생성 + 결제 승인 (기존 메서드)
    @Transactional
    public List<Order> createOrder(OrderRequest orderRequest) {
        // 4-1. 먼저 결제 승인 처리
        PaymentConfirmRequest paymentConfirmRequest = new PaymentConfirmRequest();
        paymentConfirmRequest.setAuthToken(orderRequest.getAuthToken());
        paymentConfirmRequest.setAuthUrl(orderRequest.getAuthUrl());
        paymentConfirmRequest.setOrderId(orderRequest.getOrderNumber());
        paymentConfirmRequest.setPrice(orderRequest.getPrice());
        paymentConfirmRequest.setMid(orderRequest.getMid());
        paymentConfirmRequest.setNetCancelUrl(orderRequest.getNetCancelUrl());
        paymentConfirmRequest.setMemberId(orderRequest.getMemberId());
        paymentConfirmRequest.setPaymentMethod(orderRequest.getPaymentMethod());
        paymentConfirmRequest.setUsedPoints(orderRequest.getUsedMileage()); // usedMileage를 usedPoints로 전달

        // 결제 승인 처리
        try {
            paymentService.confirmPayment(paymentConfirmRequest);
        } catch (Exception e) {
            throw OrderException.paymentFailed(e.getMessage());
        }

        // 4-2. 결제 승인 성공 시 주문 생성 (상품별로 각각 생성)
        // 주문 생성 실패 시 망취소를 위한 정보 보관
        String netCancelUrl = orderRequest.getNetCancelUrl();
        String authToken = orderRequest.getAuthToken();
        
        try {
            log.info("Processing {} order items", orderRequest.getItems() != null ? orderRequest.getItems().size() : 0);
            
            List<Order> orders = new ArrayList<>();
            List<OrderItem> orderItems = new ArrayList<>();
            int ordSeq = 1;
            
            for (OrderItemRequest itemRequest : orderRequest.getItems()) {
                log.info("Processing item: productId={}, quantity={}", itemRequest.getProductId(), itemRequest.getQuantity());
                Product product = productService.getProductById(itemRequest.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found with id " + itemRequest.getProductId()));

                // 상품별로 별도의 Order 생성
                Order order = new Order();
                order.setOrderId(orderRequest.getOrderNumber()); // 주문번호를 orderId로 설정
                order.setOrdSeq(ordSeq); // 상품별로 1, 2, 3...
                order.setOrdProcSeq(1); // 주문 시에는 항상 1
                // claim_id는 주문 취소/클레임 시에만 사용 (현재는 NULL)
                order.setMemberId(orderRequest.getMemberId());
                order.setOrderDate(LocalDateTime.now());
                order.setTotalAmount(product.getPrice() * itemRequest.getQuantity()); // 상품별 총액
                order.setStatus("ORDERED"); // 주문 완료 상태
                
                orders.add(order);

                // OrderItem 생성
                OrderItem orderItem = new OrderItem();
                orderItem.setOrderId(orderRequest.getOrderNumber());
                orderItem.setOrdSeq(ordSeq);
                orderItem.setOrdProcSeq(1);
                orderItem.setProductId(itemRequest.getProductId());
                orderItem.setQuantity(itemRequest.getQuantity());
                orderItem.setPriceAtOrder(product.getPrice());
                
                orderItems.add(orderItem);
                ordSeq++;
            }

            // 적립금 사용 처리는 Payment 테이블에서 별도로 관리

            // 주문 저장 (상품별로 각각 저장)
            log.info("Saving {} orders to database", orders.size());
            for (Order order : orders) {
                log.info("Inserting order: orderId={}, ordSeq={}, totalAmount={}", 
                        order.getOrderId(), order.getOrdSeq(), order.getTotalAmount());
                orderMapper.insert(order);
            }

            // 주문 상품 저장
            log.info("Saving {} order items to database", orderItems.size());
            for (OrderItem item : orderItems) {
                log.info("Inserting order item: orderId={}, ordSeq={}, productId={}, quantity={}", 
                        item.getOrderId(), item.getOrdSeq(), item.getProductId(), item.getQuantity());
                orderItemMapper.insert(item);
            }

            return orders; // 전체 주문 목록 반환
        } catch (Exception e) {
            log.error("Order creation failed after payment approval: {}", e.getMessage(), e);
            
            // 주문 생성 실패 시 망취소 처리
            if (netCancelUrl != null && authToken != null) {
                log.info("Attempting net cancel due to order creation failure - orderNumber: {}", orderRequest.getOrderNumber());
                try {
                    PaymentGatewayAdapter adapter = paymentGatewayFactory.getAdapter(orderRequest.getPaymentMethod());

                    PaymentNetCancelRequest paymentNetCancelRequest = new PaymentNetCancelRequest();
                    paymentNetCancelRequest.setOrderNumber(orderRequest.getOrderNumber());
                    paymentNetCancelRequest.setAuthToken(orderRequest.getAuthToken());
                    paymentNetCancelRequest.setNetCancelUrl(orderRequest.getNetCancelUrl());

                    adapter.netCancel(paymentNetCancelRequest);
                } catch (Exception netCancelException) {
                    log.error("Net cancel also failed after order creation failure: {}", netCancelException.getMessage(), netCancelException);
                }
            } else {
                log.warn("Cannot perform net cancel - missing netCancelUrl or authToken");
            }
            
            // 원래 예외를 다시 던짐 (트랜잭션 롤백)
            throw new RuntimeException("Order creation failed after payment approval: " + e.getMessage(), e);
        }
    }
}
