package com.api.app.entity.basketbase;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BasketBaseDto {
    private String basketNo;
    private String memberNo;
    private String goodsNo;
    private String itemNo;
    private Long quantity;
    private Boolean isOrder;
    private String registId;
    private LocalDateTime registDateTime;
    private String modifyId;
    private LocalDateTime modifyDateTime;
}
