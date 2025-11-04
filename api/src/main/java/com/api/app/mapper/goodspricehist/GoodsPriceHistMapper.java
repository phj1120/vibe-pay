package com.api.app.mapper.goodspricehist;

import com.api.app.entity.goodspricehist.GoodsPriceHistDto;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface GoodsPriceHistMapper {
    void insertGoodsPriceHist(GoodsPriceHistDto goodsPriceHistDto);
    List<GoodsPriceHistDto> selectGoodsPriceHistList();
    GoodsPriceHistDto selectGoodsPriceHist(String goodsNo, LocalDate startDateTime, LocalDate endDateTime);
    void updateGoodsPriceHist(GoodsPriceHistDto goodsPriceHistDto);
    void deleteGoodsPriceHist(String goodsNo, LocalDate startDateTime, LocalDate endDateTime);
}
