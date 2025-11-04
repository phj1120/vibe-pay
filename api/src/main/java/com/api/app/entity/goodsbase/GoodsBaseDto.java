package com.api.app.entity.goodsbase;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GoodsBaseDto {
    private String goodsNo;
    private String goodsName;
    private String goodsStatusCode;
    private String goodsMainImageUrl;
    private String registId;
    private LocalDateTime registDateTime;
    private String modifyId;
    private LocalDateTime modifyDateTime;
}
