package com.api.app.mapper.orderdetail;

import com.api.app.entity.orderdetail.OrderDetailDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderDetailMapper {
    void insertOrderDetail(OrderDetailDto orderDetailDto);
    List<OrderDetailDto> selectOrderDetailList();
    OrderDetailDto selectOrderDetail(String orderNo, Long orderSequence, Long orderProcessSequence);
    void updateOrderDetail(OrderDetailDto orderDetailDto);
    void deleteOrderDetail(String orderNo, Long orderSequence, Long orderProcessSequence);
}
