package com.api.app.mapper.paybase;

import com.api.app.entity.paybase.PayBaseDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PayBaseMapper {
    void insertPayBase(PayBaseDto payBaseDto);
    List<PayBaseDto> selectPayBaseList();
    PayBaseDto selectPayBase(String payNo);
    void updatePayBase(PayBaseDto payBaseDto);
    void deletePayBase(String payNo);
}
