package com.api.app.point.request;

import lombok.Data;

@Data
public class PointTransactionRequestDto {
    private Long amount;
    private String pointTransactionCode;
    private String pointTransactionReasonCode;
    private String pointTransactionReasonNo;
}
