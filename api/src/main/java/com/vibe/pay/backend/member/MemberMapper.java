package com.vibe.pay.backend.member;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
@Mapper
public interface MemberMapper {
    void insert(Member member);

    Member selectById(Long memberId);

    List<Member> selectAll();

    void update(Member member);

    void delete(Long memberId);
}
