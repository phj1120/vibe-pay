package com.api.app.mapper.basketbase;

import com.api.app.entity.basketbase.BasketBaseDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BasketBaseMapper {
    void insertBasketBase(BasketBaseDto basketBaseDto);
    List<BasketBaseDto> selectBasketBaseList();
    BasketBaseDto selectBasketBase(String basketNo);
    void updateBasketBase(BasketBaseDto basketBaseDto);
    void deleteBasketBase(String basketNo);
}
