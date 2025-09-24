package com.vibe.pay.backend.order;

import com.vibe.pay.backend.order.command.*;
import com.vibe.pay.backend.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceWithCommand {

    private final OrderService orderService;
    private final PaymentService paymentService;
    private final OrderCommandInvoker commandInvoker;

    /**
     * Command 패턴을 사용한 주문 생성
     */
    public Order createOrderWithCommand(OrderRequest orderRequest) {
        log.info("Creating order using Command pattern: {}", orderRequest.getOrderNumber());

        CreateOrderCommand command = new CreateOrderCommand(orderRequest, orderService, paymentService);
        return commandInvoker.execute(command);
    }

    /**
     * Command 패턴을 사용한 주문 취소
     */
    public Order cancelOrderWithCommand(String orderId) {
        log.info("Cancelling order using Command pattern: {}", orderId);

        CancelOrderCommand command = new CancelOrderCommand(orderId, orderService);
        return commandInvoker.execute(command);
    }

    /**
     * 마지막 주문 작업 취소 (가능한 경우)
     */
    public void undoLastOrder() {
        log.info("Attempting to undo last order operation");
        commandInvoker.undoLast();
    }

    /**
     * 주문 워크플로우 - 여러 단계를 순차 실행
     */
    public Order executeOrderWorkflow(OrderRequest orderRequest) {
        log.info("Executing order workflow: {}", orderRequest.getOrderNumber());

        try {
            // 1. 주문 생성
            Order order = createOrderWithCommand(orderRequest);

            // 2. 추가 비즈니스 로직이 필요한 경우 여기에 추가
            // 예: 재고 감소, 이메일 발송 등

            log.info("Order workflow completed successfully: {}", order.getOrderId());
            return order;

        } catch (Exception e) {
            log.error("Order workflow failed: {}", e.getMessage(), e);

            // 실패 시 자동으로 이전 작업 롤백 시도
            try {
                undoLastOrder();
                log.info("Successfully rolled back failed order workflow");
            } catch (Exception undoException) {
                log.error("Failed to rollback order workflow: {}", undoException.getMessage(), undoException);
            }

            throw e;
        }
    }

    /**
     * 명령 히스토리 조회
     */
    public int getCommandHistorySize() {
        return commandInvoker.getCommandHistory().size();
    }

    /**
     * 명령 히스토리 클리어
     */
    public void clearCommandHistory() {
        commandInvoker.clearHistory();
    }
}