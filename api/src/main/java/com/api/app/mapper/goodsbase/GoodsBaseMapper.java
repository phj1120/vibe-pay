package com.api.app.mapper.goodsbase;

import com.api.app.entity.goodsbase.GoodsBaseDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface GoodsBaseMapper {
    void insertGoodsBase(GoodsBaseDto goodsBaseDto);
    List<GoodsBaseDto> selectGoodsBaseList();
    GoodsBaseDto selectGoodsBase(String goodsNo);
    void updateGoodsBase(GoodsBaseDto goodsBaseDto);
    void deleteGoodsBase(String goodsNo);
}
