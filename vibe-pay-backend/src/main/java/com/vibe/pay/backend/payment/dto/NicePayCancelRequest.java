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
public class NicePayCancelRequest {
    private String TID;
    private String MID;
    private String CancelAmt;
    private String CancelMsg;
    private String PartialCancelCode;
    private String EdiDate;
    private String SignData;

    private String Moid;
    private String CharSet;
    private String EdiType;
    private String MallReserved;
}