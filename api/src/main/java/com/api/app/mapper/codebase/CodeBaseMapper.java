package com.api.app.mapper.codebase;

import com.api.app.entity.codebase.CodeBaseDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CodeBaseMapper {
    void insertCodeBase(CodeBaseDto codeBaseDto);
    List<CodeBaseDto> selectCodeBaseList();
    CodeBaseDto selectCodeBase(String groupCode);
    void updateCodeBase(CodeBaseDto codeBaseDto);
    void deleteCodeBase(String groupCode);
}
