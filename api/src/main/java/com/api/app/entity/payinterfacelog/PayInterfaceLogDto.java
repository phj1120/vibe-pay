package com.api.app.entity.payinterfacelog;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PayInterfaceLogDto {
    private String payInterfaceNo;
    private String memberNo;
    private String payNo;
    private String payLogCode;
    private String requestJson;
    private String responseJson;
    private String registId;
    private LocalDateTime registDateTime;
    private String modifyId;
    private LocalDateTime modifyDateTime;
}
