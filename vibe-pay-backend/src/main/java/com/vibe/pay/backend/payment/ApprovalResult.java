package com.vibe.pay.backend.payment;

/**
 * ¹x °ü| ô” t˜¤
 */
public class ApprovalResult {
    private final boolean success;
    private final String responseBody;
    private final String transactionId;

    public ApprovalResult(boolean success, String responseBody, String transactionId) {
        this.success = success;
        this.responseBody = responseBody;
        this.transactionId = transactionId;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public String getTransactionId() {
        return transactionId;
    }
}