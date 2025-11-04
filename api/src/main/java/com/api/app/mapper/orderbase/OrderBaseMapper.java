package com.api.app.mapper.orderbase;

import com.api.app.entity.orderbase.OrderBaseDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderBaseMapper {
    void insertOrderBase(OrderBaseDto orderBaseDto);
    List<OrderBaseDto> selectOrderBaseList();
    OrderBaseDto selectOrderBase(String orderNo);
    void updateOrderBase(OrderBaseDto orderBaseDto);
    void deleteOrderBase(String orderNo);
}
