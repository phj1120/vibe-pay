package com.vibe.pay.backend.member;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface MemberMapper {
    List<Member> findAll();
    Member findByMemberId(Long memberId);
    Member findByName(String name);
    Member findByEmail(String email);
    Member findByPhoneNumber(String phoneNumber);
    void insert(Member member);
    void update(Member member);
    void delete(Long memberId);
}