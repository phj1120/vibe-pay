package com.vibe.pay.backend.common;

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
