package com.api.app.goods.request;

import lombok.Data;

import java.util.List;

@Data
public class GoodsRegisterRequestDto {
    private String goodsName;
    private String goodsMainImageUrl;
    private Long salePrice;
    private Long supplyPrice;
    private List<GoodsItemRequestDto> items;
}
