package com.vibe.pay.backend.common;

import java.time.Instant;

public class ErrorResponse {
    private final Instant timestamp = Instant.now();
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final String traceId;

    public ErrorResponse(int status, String error, String message, String path, String traceId) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.traceId = traceId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public String getTraceId() {
        return traceId;
    }
}
