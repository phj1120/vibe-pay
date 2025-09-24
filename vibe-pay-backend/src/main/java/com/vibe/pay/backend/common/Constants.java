package com.vibe.pay.backend.common;

import java.time.format.DateTimeFormatter;

public final class Constants {

    private Constants() {
        // Utility class
    }

    // Date Format Patterns
    public static final String DATE_PATTERN_YYYYMMDD = "yyyyMMdd";
    public static final String DATETIME_PATTERN_YYYYMMDDHHMMSS = "yyyyMMddHHmmss";

    // DateTimeFormatters (thread-safe)
    public static final DateTimeFormatter DATE_FORMATTER_YYYYMMDD = DateTimeFormatter.ofPattern(DATE_PATTERN_YYYYMMDD);
    public static final DateTimeFormatter DATETIME_FORMATTER_YYYYMMDDHHMMSS = DateTimeFormatter.ofPattern(DATETIME_PATTERN_YYYYMMDDHHMMSS);

    // ID Generation
    public static final String ORDER_ID_PREFIX = "";
    public static final String PAYMENT_ID_PREFIX = "P";
    public static final String CLAIM_ID_PREFIX = "C";
    public static final int SEQUENCE_PADDING_LENGTH = 8;

    // Payment Status
    public static final String PAYMENT_STATUS_SUCCESS = "SUCCESS";
    public static final String PAYMENT_STATUS_FAILED = "FAILED";
    public static final String PAYMENT_STATUS_CANCELLED = "CANCELLED";
    public static final String PAYMENT_STATUS_PENDING = "PENDING";

    // Order Status
    public static final String ORDER_STATUS_ORDER = "ORDER";
    public static final String ORDER_STATUS_CANCELED = "CANCELED";

    // Payment Types
    public static final String PAY_TYPE_PAYMENT = "PAYMENT";
    public static final String PAY_TYPE_REFUND = "REFUND";

    // Payment Methods
    public static final String PAYMENT_METHOD_CREDIT_CARD = "CREDIT_CARD";
    public static final String PAYMENT_METHOD_POINT = "POINT";

    // PG Companies
    public static final String PG_COMPANY_INICIS = "INICIS";
    public static final String PG_COMPANY_NICEPAY = "NICEPAY";
    public static final String PG_COMPANY_TOSS = "TOSS";

    // API Response Codes
    public static final String RESPONSE_CODE_SUCCESS = "0000";

    // Default Values
    public static final String DEFAULT_PHONE_NUMBER = "010-0000-0000";
    public static final String DEFAULT_EMAIL = "buyer@example.com";
    public static final String DEFAULT_BUYER_NAME = "l‰ê";
    public static final String DEFAULT_PRODUCT_NAME = "¸8∞";
}