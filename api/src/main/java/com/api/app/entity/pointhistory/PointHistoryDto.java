package com.api.app.entity.pointhistory;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PointHistoryDto {
    private String pointHistoryNo;
    private String memberNo;
    private String amount;
    private String pointTransactionCode;
    private String pointTransactionResonCode;
    private String pointTransactionResonNo;
    private LocalDate startDateTime;
    private LocalDate endDateTime;
    private String upperPointHistoryNo;
    private Long remainPoint;
    private String registId;
    private LocalDateTime registDateTime;
    private String modifyId;
    private LocalDateTime modifyDateTime;
}
