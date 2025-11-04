package com.api.app.entity.goodsitem;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GoodsItemDto {
    private String goodsNo;
    private String itemNo;
    private String itemName;
    private Long itemPrice;
    private Long stock;
    private String goodsStatusCode;
    private String registId;
    private LocalDateTime registDateTime;
    private String modifyId;
    private LocalDateTime modifyDateTime;
}
