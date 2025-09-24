package com.vibe.pay.backend.order.command;

import com.vibe.pay.backend.order.Order;

public interface OrderCommand {
    Order execute();
    void undo();
    boolean canUndo();
    String getCommandType();
}