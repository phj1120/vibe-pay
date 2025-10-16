package com.vibe.pay.common;

import java.time.format.DateTimeFormatter;

public class Constants {
    public static final DateTimeFormatter DATE_FORMATTER_YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");
    public static final int SEQUENCE_PADDING_LENGTH = 8;

    private Constants() {
        // Utility class
    }
}
