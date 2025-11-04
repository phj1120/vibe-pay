package com.api.app.entity.orderdetail;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class OrderDetailDto {
    private String orderNo;
    private Long orderSequence;
    private Long orderProcessSequence;
    private Long upperOrderProcessSequence;
    private String claimNo;
    private String goodsNo;
    private String itemNo;
    private Long quantity;
    private String orderStatusCode;
    private String deliveryTypeCode;
    private String orderTypeCode;
    private LocalDate orderAcceptDtm;
    private LocalDate orderFinishDtm;
    private String registId;
    private LocalDateTime registDateTime;
    private String modifyId;
    private LocalDateTime modifyDateTime;
}
