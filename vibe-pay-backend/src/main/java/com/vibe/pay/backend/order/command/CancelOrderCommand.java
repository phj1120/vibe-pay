package com.vibe.pay.backend.order.command;

import com.vibe.pay.backend.order.Order;
import com.vibe.pay.backend.order.OrderService;
import com.vibe.pay.backend.exception.OrderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CancelOrderCommand implements OrderCommand {

    private static final Logger log = LoggerFactory.getLogger(CancelOrderCommand.class);

    private final String orderId;
    private final OrderService orderService;
    private Order cancelledOrder;
    private boolean executed = false;

    public CancelOrderCommand(String orderId, OrderService orderService) {
        this.orderId = orderId;
        this.orderService = orderService;
    }

    @Override
    public Order execute() {
        try {
            log.info("Executing cancel order command for orderId: {}", orderId);

            cancelledOrder = orderService.cancelOrder(orderId);
            executed = true;

            log.info("Cancel order command executed successfully for orderId: {}", orderId);
            return cancelledOrder;

        } catch (Exception e) {
            log.error("Failed to execute cancel order command: {}", e.getMessage(), e);
            throw OrderException.alreadyCancelled(orderId);
        }
    }

    @Override
    public void undo() {
        // 주문 취소의 undo는 지원하지 않음 (비즈니스 규칙)
        throw new UnsupportedOperationException("Cannot undo order cancellation");
    }

    @Override
    public boolean canUndo() {
        return false; // 주문 취소는 되돌릴 수 없음
    }

    @Override
    public String getCommandType() {
        return "CANCEL_ORDER";
    }
}