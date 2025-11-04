package com.api.app.mapper.memberbase;

import com.api.app.entity.memberbase.MemberBaseDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MemberBaseMapper {
    void insertMemberBase(MemberBaseDto memberBaseDto);
    List<MemberBaseDto> selectMemberBaseList();
    MemberBaseDto selectMemberBase(String memberNo);
    void updateMemberBase(MemberBaseDto memberBaseDto);
    void deleteMemberBase(String memberNo);
}
