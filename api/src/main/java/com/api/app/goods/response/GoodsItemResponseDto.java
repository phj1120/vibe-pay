package com.api.app.goods.response;

import lombok.Data;

@Data
public class GoodsItemResponseDto {
    private String goodsNo;
    private String itemNo;
    private String itemName;
    private Long itemPrice;
    private Long stock;
    private String goodsStatusCode;
}
