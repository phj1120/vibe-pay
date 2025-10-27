package com.vibe.pay.backend.exception;

/**
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String message) {
        super(message);
    }
}
