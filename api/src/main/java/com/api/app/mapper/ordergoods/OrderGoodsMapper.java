package com.api.app.mapper.ordergoods;

import com.api.app.entity.ordergoods.OrderGoodsDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderGoodsMapper {
    void insertOrderGoods(OrderGoodsDto orderGoodsDto);
    List<OrderGoodsDto> selectOrderGoodsList();
    OrderGoodsDto selectOrderGoods(String orderNo, String goodsNo, String itemNo);
    void updateOrderGoods(OrderGoodsDto orderGoodsDto);
    void deleteOrderGoods(String orderNo, String goodsNo, String itemNo);
}
