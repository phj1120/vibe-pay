package com.api.app.mapper.codedetail;

import com.api.app.entity.codedetail.CodeDetailDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CodeDetailMapper {
    void insertCodeDetail(CodeDetailDto codeDetailDto);
    List<CodeDetailDto> selectCodeDetailList();
    CodeDetailDto selectCodeDetail(String groupCode, String code);
    void updateCodeDetail(CodeDetailDto codeDetailDto);
    void deleteCodeDetail(String groupCode, String code);
}
