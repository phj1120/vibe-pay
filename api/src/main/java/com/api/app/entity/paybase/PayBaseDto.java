package com.api.app.entity.paybase;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PayBaseDto {
    private String payNo;
    private String payTypeCode;
    private String payWayCode;
    private String payStatusCode;
    private String approveNo;
    private String orderNo;
    private String claimNo;
    private String upperPayNo;
    private String trdNo;
    private LocalDate payFinishDateTime;
    private String memberNo;
    private Long amount;
    private Long cancelableAmount;
    private String pgTypeCode;
    private String registId;
    private LocalDateTime registDateTime;
    private String modifyId;
    private LocalDateTime modifyDateTime;
}
