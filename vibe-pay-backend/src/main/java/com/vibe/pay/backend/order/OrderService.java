package com.vibe.pay.backend.order;

import com.vibe.pay.backend.payment.Payment;
import com.vibe.pay.backend.payment.PaymentService;
import com.vibe.pay.backend.payment.PaymentMapper;
import com.vibe.pay.backend.payment.PaymentConfirmRequest;
import com.vibe.pay.backend.product.Product;
import com.vibe.pay.backend.product.ProductService;
import com.vibe.pay.backend.rewardpoints.RewardPointsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private ProductService productService;

    @Autowired
    private RewardPointsService rewardPointsService;

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private PaymentService paymentService;


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
        // 1. 원본 주문 조회 (ord_proc_seq = 1인 원본만)
        Order originalOrder = orderMapper.findByOrderIdAndOrdProcSeq(orderId, 1);
        if (originalOrder == null) {
            throw new RuntimeException("Order not found with id " + orderId);
        }

        // 2. 이미 취소된 주문인지 확인 (ord_proc_seq가 1보다 큰 경우 이미 취소됨)
        if (originalOrder.getOrdProcSeq() > 1) {
            throw new RuntimeException("Order is already cancelled");
        }

        // 3. 클레임 번호 생성
        String claimId = generateClaimNumber();

        // 4. 취소건 주문 생성 (ord_proc_seq +1)
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

        // 6. 취소건 주문 상품들 생성
        List<OrderItem> originalOrderItems = orderItemMapper.findByOrderId(orderId);
        for (OrderItem originalItem : originalOrderItems) {
            OrderItem cancelItem = new OrderItem();
            cancelItem.setOrderId(cancelOrder.getOrderId());
            cancelItem.setOrdSeq(originalItem.getOrdSeq());
            cancelItem.setOrdProcSeq(cancelOrder.getOrdProcSeq()); // 취소건의 ord_proc_seq 사용
            cancelItem.setProductId(originalItem.getProductId());
            cancelItem.setQuantity(-originalItem.getQuantity()); // 마이너스 수량
            cancelItem.setPriceAtOrder(originalItem.getPriceAtOrder());
            
            orderItemMapper.insert(cancelItem);
        }

        // 7. 결제 취소 처리 (포인트 환불은 한 번만 처리)
        List<Payment> payments = paymentService.findByOrderId(orderId);
        boolean pointRefundProcessed = false;
        if (!payments.isEmpty()) {
            for (Payment payment : payments) {
                // 포인트 사용 결제인 경우 첫 번째 건에서만 포인트 환불 처리
                if ("POINT".equals(payment.getPaymentMethod()) && !pointRefundProcessed) {
                    paymentService.cancelPayment(payment.getPaymentId());
                    pointRefundProcessed = true;
                } else if (!"POINT".equals(payment.getPaymentMethod())) {
                    // 카드 결제 등 PG 결제는 정상 취소 처리
                    paymentService.cancelPayment(payment.getPaymentId());
                }
            }
        }

        // 8. 포인트 환불 처리는 Payment 취소에서 자동으로 처리됨

        return cancelOrder; // 취소건 주문 반환
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
    
    // 2. 클레임 번호 채번 (날짜 + C + 시퀀스)
    public String generateClaimNumber() {
        // 현재 날짜를 YYYYMMDD 형식으로 포맷
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        // DB 시퀀스에서 다음 번호 조회
        Long sequence = orderMapper.getNextClaimSequence();
        
        // 8자리로 패딩 (00000001 ~ 99999999)
        String sequenceStr = String.format("%08d", sequence);
        
        return dateStr + "C" + sequenceStr;
    }

    // 주문 생성 + 결제 승인
    @Transactional
    public Order createOrder(OrderRequest orderRequest) {
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
        Payment payment;
        try {
            payment = paymentService.confirmPayment(paymentConfirmRequest);
        } catch (Exception e) {
            throw new RuntimeException("Payment approval failed: " + e.getMessage(), e);
        }

        // 4-2. 결제 승인 성공 시 주문 생성
        Order order = new Order();
        order.setOrderId(orderRequest.getOrderNumber()); // 주문번호를 orderId로 설정
        order.setOrdSeq(1); // 첫 번째 주문은 항상 1
        order.setOrdProcSeq(1); // 주문 시에는 항상 1
        // claim_id는 주문 취소/클레임 시에만 사용 (현재는 NULL)
        order.setMemberId(orderRequest.getMemberId());

        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PAID"); // 결제 완료 상태

        // 주문 상품 정보 처리 및 총액 계산
        log.info("Processing {} order items", orderRequest.getItems() != null ? orderRequest.getItems().size() : 0);
        double calculatedTotalAmount = 0.0;
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemRequest itemRequest : orderRequest.getItems()) {
            log.info("Processing item: productId={}, quantity={}", itemRequest.getProductId(), itemRequest.getQuantity());
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

        // 적립금 사용 처리는 Payment 테이블에서 별도로 관리

        // 주문 저장
        orderMapper.insert(order);

        // 주문 상품 저장
        log.info("Saving {} order items to database", orderItems.size());
        int seqCounter = 1;
        for (OrderItem item : orderItems) {
            item.setOrderId(order.getOrderId());
            item.setOrdSeq(seqCounter++); // 1, 2, 3...
            item.setOrdProcSeq(1);
            log.info("Inserting order item: orderId={}, ordSeq={}, productId={}, quantity={}", 
                    item.getOrderId(), item.getOrdSeq(), item.getProductId(), item.getQuantity());
            orderItemMapper.insert(item);
        }

        return order;
    }
}
