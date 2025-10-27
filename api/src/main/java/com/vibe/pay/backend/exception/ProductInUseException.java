package com.vibe.pay.backend.exception;

/**
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
public class ProductInUseException extends RuntimeException {
    public ProductInUseException(String message) {
        super(message);
    }
}
