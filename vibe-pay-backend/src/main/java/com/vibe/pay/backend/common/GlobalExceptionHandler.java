package com.vibe.pay.backend.common;

import com.vibe.pay.backend.exception.BusinessException;
import com.vibe.pay.backend.exception.PaymentException;
import com.vibe.pay.backend.exception.OrderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private String genTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class,
            HttpMessageNotReadableException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex, HttpServletRequest request) {
        String traceId = genTraceId();
        log.warn("Bad request at {} {} traceId={}", request.getMethod(), request.getRequestURI(), traceId, ex);
        ErrorResponse body = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI(),
                traceId
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // 비즈니스 예외 처리
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        String traceId = genTraceId();
        log.warn("Business exception at {} {} traceId={} errorCode={}",
                request.getMethod(), request.getRequestURI(), traceId, ex.getErrorCode(), ex);

        ErrorResponse body = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getErrorCode(),
                ex.getErrorMessage(),
                request.getRequestURI(),
                traceId
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // 결제 예외 처리
    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ErrorResponse> handlePaymentException(PaymentException ex, HttpServletRequest request) {
        String traceId = genTraceId();
        log.error("Payment exception at {} {} traceId={}",
                request.getMethod(), request.getRequestURI(), traceId, ex);

        ErrorResponse body = new ErrorResponse(
                HttpStatus.PAYMENT_REQUIRED.value(),
                ex.getErrorCode(),
                ex.getErrorMessage(),
                request.getRequestURI(),
                traceId
        );
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(body);
    }

    // 주문 예외 처리
    @ExceptionHandler(OrderException.class)
    public ResponseEntity<ErrorResponse> handleOrderException(OrderException ex, HttpServletRequest request) {
        String traceId = genTraceId();
        log.error("Order exception at {} {} traceId={}",
                request.getMethod(), request.getRequestURI(), traceId, ex);

        ErrorResponse body = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getErrorCode(),
                ex.getErrorMessage(),
                request.getRequestURI(),
                traceId
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex, HttpServletRequest request) {
        String traceId = genTraceId();
        log.error("Unhandled exception at {} {} traceId={}", request.getMethod(), request.getRequestURI(), traceId, ex);
        ErrorResponse body = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                ex.getMessage(),
                request.getRequestURI(),
                traceId
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
