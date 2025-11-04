package com.api.app.goods.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GoodsDetailDto {
    private String goodsNo;
    private String goodsName;
    private String goodsStatusCode;
    private String goodsMainImageUrl;
    private Long salePrice;
    private Long supplyPrice;
    private List<GoodsItemResponseDto> items;
    private LocalDateTime registDateTime;
    private LocalDateTime modifyDateTime;
}
