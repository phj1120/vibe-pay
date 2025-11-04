package com.api.app.entity.orderbase;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderBaseDto {
    private String orderNo;
    private String memberNo;
    private String registId;
    private LocalDateTime registDateTime;
    private String modifyId;
    private LocalDateTime modifyDateTime;
}
