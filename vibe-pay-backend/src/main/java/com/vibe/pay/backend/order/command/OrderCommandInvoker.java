package com.vibe.pay.backend.order.command;

import com.vibe.pay.backend.order.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrderCommandInvoker {

    private static final Logger log = LoggerFactory.getLogger(OrderCommandInvoker.class);
    private final List<OrderCommand> commandHistory = new ArrayList<>();

    /**
     * 명령 실행
     */
    public Order execute(OrderCommand command) {
        try {
            log.info("Executing command: {}", command.getCommandType());

            Order result = command.execute();
            commandHistory.add(command);

            log.info("Command executed successfully: {}", command.getCommandType());
            return result;

        } catch (Exception e) {
            log.error("Command execution failed: {} - {}", command.getCommandType(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 마지막 명령 취소
     */
    public void undoLast() {
        if (commandHistory.isEmpty()) {
            log.warn("No commands to undo");
            return;
        }

        OrderCommand lastCommand = commandHistory.get(commandHistory.size() - 1);

        if (!lastCommand.canUndo()) {
            log.warn("Last command cannot be undone: {}", lastCommand.getCommandType());
            throw new UnsupportedOperationException("Command cannot be undone: " + lastCommand.getCommandType());
        }

        try {
            log.info("Undoing last command: {}", lastCommand.getCommandType());
            lastCommand.undo();
            commandHistory.remove(lastCommand);
            log.info("Command undone successfully: {}", lastCommand.getCommandType());

        } catch (Exception e) {
            log.error("Failed to undo command: {} - {}", lastCommand.getCommandType(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 특정 타입의 마지막 명령 찾기
     */
    public OrderCommand findLastCommand(String commandType) {
        for (int i = commandHistory.size() - 1; i >= 0; i--) {
            OrderCommand command = commandHistory.get(i);
            if (commandType.equals(command.getCommandType())) {
                return command;
            }
        }
        return null;
    }

    /**
     * 명령 히스토리 조회
     */
    public List<OrderCommand> getCommandHistory() {
        return new ArrayList<>(commandHistory);
    }

    /**
     * 명령 히스토리 클리어
     */
    public void clearHistory() {
        log.info("Clearing command history: {} commands", commandHistory.size());
        commandHistory.clear();
    }
}