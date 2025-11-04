package com.api.app.entity.goodspricehist;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class GoodsPriceHistDto {
    private String goodsNo;
    private LocalDate startDateTime;
    private LocalDate endDateTime;
    private Long salePrice;
    private Long supplyPrice;
    private String registId;
    private LocalDateTime registDateTime;
    private String modifyId;
    private LocalDateTime modifyDateTime;
}
