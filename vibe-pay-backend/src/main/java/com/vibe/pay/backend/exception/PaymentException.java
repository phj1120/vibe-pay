package com.vibe.pay.backend.exception;

public class PaymentException extends BusinessException {

    public PaymentException(String errorMessage) {
        super("PAYMENT_ERROR", errorMessage);
    }

    public PaymentException(String errorMessage, Throwable cause) {
        super("PAYMENT_ERROR", errorMessage, cause);
    }

    // 특정 결제 에러 코드들
    public static PaymentException insufficientBalance() {
        return new PaymentException("잔액이 부족합니다");
    }

    public static PaymentException invalidPaymentMethod() {
        return new PaymentException("지원하지 않는 결제 수단입니다");
    }

    public static PaymentException pgSystemError(String pgName, Throwable cause) {
        return new PaymentException(pgName + " PG 시스템 오류가 발생했습니다", cause);
    }

    public static PaymentException approvalFailed(String reason) {
        return new PaymentException("결제 승인이 실패했습니다: " + reason);
    }

    public static PaymentException initiationFailed(String reason) {
        return new PaymentException("결제 시작이 실패했습니다: " + reason);
    }
}