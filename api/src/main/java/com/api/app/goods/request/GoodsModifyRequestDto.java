package com.api.app.goods.request;

import lombok.Data;

import java.util.List;

@Data
public class GoodsModifyRequestDto {
    private String goodsNo;
    private String goodsName;
    private String goodsStatusCode;
    private String goodsMainImageUrl;
    private Long salePrice;
    private Long supplyPrice;
    private List<GoodsItemRequestDto> items;
}
