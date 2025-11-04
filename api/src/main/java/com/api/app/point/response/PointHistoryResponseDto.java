package com.api.app.point.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PointHistoryResponseDto {
    private String pointHistoryNo;
    private String memberNo;
    private Long amount;
    private String pointTransactionCode;
    private String pointTransactionReasonCode;
    private String pointTransactionReasonNo;
    private LocalDate startDateTime;
    private LocalDate endDateTime;
    private String upperPointHistoryNo;
    private Long remainPoint;
    private LocalDateTime registDateTime;
}
