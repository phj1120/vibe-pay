package com.api.app.entity.ordergoods;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderGoodsDto {
    private String orderNo;
    private String goodsNo;
    private String itemNo;
    private Long salePrice;
    private Long supplyPrice;
    private String goodsName;
    private String itemName;
    private String registId;
    private LocalDateTime registDateTime;
    private String modifyId;
    private LocalDateTime modifyDateTime;
}
