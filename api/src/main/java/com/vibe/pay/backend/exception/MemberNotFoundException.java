package com.vibe.pay.backend.exception;

/**
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
public class MemberNotFoundException extends RuntimeException {
    public MemberNotFoundException(String message) {
        super(message);
    }
}
