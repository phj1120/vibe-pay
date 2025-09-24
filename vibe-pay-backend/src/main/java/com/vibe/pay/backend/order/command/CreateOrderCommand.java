package com.vibe.pay.backend.order.command;

import com.vibe.pay.backend.order.Order;
import com.vibe.pay.backend.order.OrderRequest;
import com.vibe.pay.backend.order.OrderService;
import com.vibe.pay.backend.payment.PaymentService;
import com.vibe.pay.backend.payment.PaymentConfirmRequest;
import com.vibe.pay.backend.exception.OrderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CreateOrderCommand implements OrderCommand {

    private static final Logger log = LoggerFactory.getLogger(CreateOrderCommand.class);

    private final OrderRequest orderRequest;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private List<Order> createdOrders;
    private boolean executed = false;

    public CreateOrderCommand(OrderRequest orderRequest, OrderService orderService, PaymentService paymentService) {
        this.orderRequest = orderRequest;
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    @Override
    public Order execute() {
        try {
            log.info("Executing create order command for orderId: {}", orderRequest.getOrderNumber());

            // 1. 결제 승인 처리
            PaymentConfirmRequest paymentConfirmRequest = buildPaymentConfirmRequest();
            paymentService.confirmPayment(paymentConfirmRequest);

            // 2. 주문 생성
            createdOrders = orderService.createOrderWithoutPayment(orderRequest);
            executed = true;

            log.info("Create order command executed successfully: {} orders created", createdOrders.size());
            return createdOrders.get(0); // 첫 번째 주문 반환

        } catch (Exception e) {
            log.error("Failed to execute create order command: {}", e.getMessage(), e);
            // 망취소 처리
            if (orderRequest.getNetCancelUrl() != null && orderRequest.getAuthToken() != null) {
                try {
                    paymentService.performNetCancel(
                        orderRequest.getNetCancelUrl(),
                        orderRequest.getAuthToken(),
                        orderRequest.getOrderNumber()
                    );
                } catch (Exception netCancelException) {
                    log.error("Net cancel failed: {}", netCancelException.getMessage(), netCancelException);
                }
            }
            throw OrderException.invalidOrderAmount();
        }
    }

    @Override
    public void undo() {
        if (!canUndo()) {
            throw new UnsupportedOperationException("Cannot undo create order command");
        }

        try {
            log.info("Undoing create order command for orderId: {}", orderRequest.getOrderNumber());

            // 주문 취소 처리
            for (Order order : createdOrders) {
                orderService.cancelOrder(order.getOrderId());
            }

            log.info("Create order command undone successfully");

        } catch (Exception e) {
            log.error("Failed to undo create order command: {}", e.getMessage(), e);
            throw new RuntimeException("Undo failed: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean canUndo() {
        return executed && createdOrders != null && !createdOrders.isEmpty();
    }

    @Override
    public String getCommandType() {
        return "CREATE_ORDER";
    }

    private PaymentConfirmRequest buildPaymentConfirmRequest() {
        PaymentConfirmRequest request = new PaymentConfirmRequest();
        request.setAuthToken(orderRequest.getAuthToken());
        request.setAuthUrl(orderRequest.getAuthUrl());
        request.setOrderId(orderRequest.getOrderNumber());
        request.setPrice(orderRequest.getPrice());
        request.setMid(orderRequest.getMid());
        request.setNetCancelUrl(orderRequest.getNetCancelUrl());
        request.setMemberId(orderRequest.getMemberId());
        request.setPaymentMethod(orderRequest.getPaymentMethod());
        request.setUsedPoints(orderRequest.getUsedMileage());
        return request;
    }
}