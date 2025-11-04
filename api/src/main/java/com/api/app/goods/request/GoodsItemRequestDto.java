package com.api.app.goods.request;

import lombok.Data;

@Data
public class GoodsItemRequestDto {
    private String itemNo;
    private String itemName;
    private Long itemPrice;
    private Long stock;
    private String goodsStatusCode;
}
