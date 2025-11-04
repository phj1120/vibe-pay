package com.api.app.mapper.goodsitem;

import com.api.app.entity.goodsitem.GoodsItemDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface GoodsItemMapper {
    void insertGoodsItem(GoodsItemDto goodsItemDto);
    List<GoodsItemDto> selectGoodsItemList();
    GoodsItemDto selectGoodsItem(String goodsNo, String itemNo);
    void updateGoodsItem(GoodsItemDto goodsItemDto);
    void deleteGoodsItem(String goodsNo, String itemNo);
}
