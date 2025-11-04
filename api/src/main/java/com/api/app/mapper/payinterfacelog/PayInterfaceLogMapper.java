package com.api.app.mapper.payinterfacelog;

import com.api.app.entity.payinterfacelog.PayInterfaceLogDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PayInterfaceLogMapper {
    void insertPayInterfaceLog(PayInterfaceLogDto payInterfaceLogDto);
    List<PayInterfaceLogDto> selectPayInterfaceLogList();
    PayInterfaceLogDto selectPayInterfaceLog(String payInterfaceNo);
    void updatePayInterfaceLog(PayInterfaceLogDto payInterfaceLogDto);
    void deletePayInterfaceLog(String payInterfaceNo);
}
