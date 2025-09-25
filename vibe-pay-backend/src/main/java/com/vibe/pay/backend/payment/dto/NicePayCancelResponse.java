package com.vibe.pay.backend.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class NicePayCancelResponse {
    private String ResultCode;
    private String ResultMsg;
    private String TID;
    private String CancelAmt;
    private String CancelDate;
    private String CancelTime;
    private String RemainAmt;
    private String MID;
    private String Moid;
    private String PayMethod;
    private String GoodsName;

    private String PartialCancelCode;

    private String MallReserved;
    private String AuthDate;
    private String AuthTime;

    private String ErrorCode;
    private String ErrorMsg;
}